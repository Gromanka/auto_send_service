package com.example.auto_send_service.service;

import com.example.auto_send_service.dto.AutoSendJournalDto;
import com.example.auto_send_service.dto.AutoSendSuspOperDto;
import com.example.auto_send_service.dto.SlkResult;
import com.example.auto_send_service.dto.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class autoSendService {

    private static final int KFM_SEND_QUEUE = 15; // C_KFM_QUEUE: очередь отправки
    private static final int VALIDATION_OK = 1;
    private static final int SLK_OK = 1;
    private static final int REQUIRED_MEMBERS = 2;

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    private final AutoSendRepository repository;
    private final checkOperation operationChecker; // бывший checkOperation

    /** Запуск по всем активным конфигурациям. */
    public void run() {
        for (AutoSendSuspOperDto cfg : repository.findActiveConfigs()) {
            try {
                run(cfg.getPSuspiciousTypeCode(), cfg.getPOperationStatus(), cfg.getPUsername());
            } catch (Exception e) {
                log.error("Ошибка автоотправки: type={}, status={}, user={}",
                        cfg.getPSuspiciousTypeCode(), cfg.getPOperationStatus(),
                        cfg.getPUsername(), e);
            }
        }
    }

    public void run(Integer typeCode, Integer status, String username) {
        Optional<String> dictError = validateDicts(typeCode, status, username);
        if (dictError.isPresent()) {
            log.warn("Автоотправка пропущена: {}", dictError.get());
            saveJournal(typeCode, status, username, 0, 0, 0, STATUS_ERROR);
            return;
        }

        List<Long> ids = repository.findSuspiciousOperationIds(typeCode, status);

        int success = 0;
        int errors = 0;
        for (Long id : ids) {
            if (processOperation(id, username)) {
                success++;
            } else {
                errors++;
            }
        }

        saveJournal(typeCode, status, username, ids.size(), success, errors,
                errors > 0 ? STATUS_ERROR : STATUS_SUCCESS);
    }

    /** @return true, если операция отправлена в очередь */
    private boolean processOperation(Long id, String username) {
        try {
            int members = repository.countMembers(id);
            ValidationResult validation = operationChecker.checkOperationValidation(id);
            SlkResult slk = operationChecker.checkOperationSlk(id);

            if (validation.getStatus() == VALIDATION_OK
                    && slk.getCode() == SLK_OK
                    && members == REQUIRED_MEMBERS) {
                repository.sendToQueue(id, username, KFM_SEND_QUEUE);
                return true;
            }else {
                repository.insertError(id, buildErrorMessage(validation, slk, members));
                return false;
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке операции id={}", id, e);
            safeInsertError(id, "Непредвиденная ошибка: " + e.getMessage());
        }
        return false;
    }

    private Optional<String> validateDicts(Integer typeCode, Integer status, String username) {
        if (!repository.operationCodeExists(typeCode)) {
            return Optional.of("Справочник кодов операций не содержит код: " + typeCode);
        }
        if (!repository.suspiciousTypeExists(status)) {
            return Optional.of("Справочник статусов подозрительных операций не содержит статус: " + status);
        }
        if (!repository.userExists(username)) {
            return Optional.of("Пользователь не найден: " + username);
        }
        return Optional.empty();
    }

    private String buildErrorMessage(ValidationResult validation, SlkResult slk, int members) {
        List<String> parts = new ArrayList<>();

        if (slk.getCode() != SLK_OK) {
            parts.add(slk.getMessage());
        }
        if (validation.getStatus() != VALIDATION_OK) {
            parts.add(buildValidationMessage(validation));
        }
        if (members != REQUIRED_MEMBERS) {
            parts.add("количество участников операции должно быть " + REQUIRED_MEMBERS
                    + ", фактически: " + members);
        }

        return parts.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("; "));
    }

    private String buildValidationMessage(ValidationResult result) {
        String base = Objects.toString(result.getErrorMessage(), "");
        List<String> errors = result.getErrors();
        if (errors == null || errors.isEmpty()) {
            return base;
        }
        String details = String.join("; ", errors);
        return base.isBlank() ? details : base + ": " + details;
    }

    private void safeInsertError(Long id, String reason) {
        try {
            repository.insertError(id, reason);
        } catch (Exception e) {
            log.error("Не удалось записать ошибку для операции id={}", id, e);
        }
    }

    private void saveJournal(Integer typeCode, Integer status, String username,
                             int all, int success, int errors, String journalStatus) {
        repository.insertJournal(AutoSendJournalDto.builder()
                .pSuspiciousTypeCode(typeCode)
                .pOperationStatus(status)
                .pUsername(username)
                .allOperations(all)
                .successOperations(success)
                .errorOperations(errors)
                .status(journalStatus)
                .build());
    }
}