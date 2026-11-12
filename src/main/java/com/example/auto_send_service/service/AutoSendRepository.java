package com.example.auto_send_service.service;

import com.example.auto_send_service.dto.AutoSendJournalDto;
import com.example.auto_send_service.dto.AutoSendSuspOperDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AutoSendRepository {

    private final JdbcTemplate jdbc;

    public List<AutoSendSuspOperDto> findActiveConfigs() {
        return jdbc.query("""
                SELECT p_suspicioustypecode, p_operationstatus, active,
                       start_date, end_date, p_username
                FROM public.tb_auto_send_susp_oper
                WHERE active = 1
                  AND CURRENT_DATE BETWEEN start_date AND end_date
                """,
                BeanPropertyRowMapper.newInstance(AutoSendSuspOperDto.class));
    }

    public List<Long> findSuspiciousOperationIds(Integer typeCode, Integer status) {
        return jdbc.queryForList("""
                SELECT id
                FROM public.tb_suspiciousoperations
                WHERE p_suspicioustypecode = ?
                  AND p_operationstatus = ?
                """,
                Long.class, typeCode, status);
    }

    public int countMembers(Long operationId) {
        return jdbc.queryForObject(
                "SELECT count(*) FROM public.tb_susp_members WHERE p_suspiciousoperationid = ?",
                Integer.class, operationId);
    }

    public boolean operationCodeExists(Integer code) {
        return exists("public.tb_dict_opercode", "p_code", String.valueOf(code));
    }

    public boolean suspiciousTypeExists(Integer status) {
        return exists("public.tb_dict_suspic_type", "p_code", String.valueOf(status));
    }

    public boolean userExists(String username) {
        return exists("public.tb_users", "p_username", username.toUpperCase());
    }

    // table и column - только константы из этого класса, не пользовательский ввод
    private boolean exists(String table, String column, Object value) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM " + table + " WHERE " + column + " = ?)",
                Boolean.class, value));
    }

    public int sendToQueue(Long operationId, String username, int queue) {
        return jdbc.update("""
                UPDATE public.tb_suspiciousoperations
                SET p_username = ?, p_sendtokfmbool = ?
                WHERE id = ?
                """, username, queue, operationId);
    }

    public Long insertJournal(AutoSendJournalDto dto) {
        return jdbc.queryForObject("""
                INSERT INTO public.tb_auto_send_journal (
                    p_suspicioustypecode, p_operationstatus, p_username,
                    all_operations, success_operations, error_operations, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                dto.getPSuspiciousTypeCode(), dto.getPOperationStatus(), dto.getPUsername(),
                dto.getAllOperations(), dto.getSuccessOperations(),
                dto.getErrorOperations(), dto.getStatus());
    }

    public Long insertError(Long operationId, String reason) {
        return jdbc.queryForObject("""
                INSERT INTO public.tb_auto_send_logs (p_suspiciousoperationid, reason)
                VALUES (?, ?)
                RETURNING id
                """,
                Long.class, operationId, reason);
    }
    public boolean isHoliday(LocalDate date) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.tb_holidays
                WHERE holiday_month = ?
                  AND holiday_day = ?
                """,
                Integer.class,
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        return count != null && count > 0;
    }
}