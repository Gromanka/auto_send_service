package com.example.auto_send_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AutoSendSuspOperDto {

    private Integer pSuspiciousTypeCode;

    private Integer pOperationStatus;

    private Integer active;

    private LocalDate startDate;

    private LocalDate endDate;

    private String pUsername;
}
