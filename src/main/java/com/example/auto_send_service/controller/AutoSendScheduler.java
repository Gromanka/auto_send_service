package com.example.auto_send_service.controller;

import com.example.auto_send_service.service.AutoSendRepository;
import com.example.auto_send_service.service.autoSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSendScheduler {

    private final autoSendService autoSendService;
    private final AutoSendRepository autoSendRepository;

    @Value("${auto-send.zone:Asia/Almaty}")
    private String schedulerZone;

    // Секунды минуты часы день месяц день_недели
    // 09:15, понедельник–пятница
    @Scheduled(
            cron = "${auto-send.cron:0 15 9 * * MON-FRI}",
            zone = "${auto-send.zone:Asia/Almaty}"
    )
    public void runByCron() {

        LocalDate today = LocalDate.now(ZoneId.of(schedulerZone));

        if (autoSendRepository.isHoliday(today)) {
            log.info("Сегодня выходной или праздничный день: {}", today);
            return;
        }

        log.info("Плановый запуск автоотправки");

        try {
            autoSendService.run();
            log.info("Плановый запуск автоотправки завершён");
        } catch (Exception e) {
            log.error("Ошибка плановой автоотправки", e);
        }
    }
}