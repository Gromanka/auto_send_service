package com.example.auto_send_service.controller;


import com.example.auto_send_service.service.autoSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/auto-send")
@RequiredArgsConstructor
public class AutoSendController {

    private final autoSendService autoSendService;

    /** Запуск по требованию с тремя параметрами. */
    @PostMapping("/run")
    public ResponseEntity<String> run(
            @RequestParam  Integer suspiciousTypeCode,
            @RequestParam  Integer operationStatus,
            @RequestParam  String username) {

        log.info("Ручной запуск автоотправки: type={}, status={}, user={}",
                suspiciousTypeCode, operationStatus, username);

        autoSendService.run(suspiciousTypeCode, operationStatus, username);
        return ResponseEntity.ok("Автоотправка выполнена, результат смотрите в журнале");
    }

    /** Запуск по всем активным конфигурациям, как по расписанию. */
    @PostMapping("/run-all")
    public ResponseEntity<String> runAll() {
        log.info("Ручной запуск автоотправки по всем конфигурациям");
        autoSendService.run();
        return ResponseEntity.ok("Автоотправка выполнена по всем активным конфигурациям");
    }
}