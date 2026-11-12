package com.example.auto_send_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoSendJournalDto {

    private OffsetDateTime journalDate;

    private Integer pSuspiciousTypeCode;

    private Integer pOperationStatus;

    private String pUsername;

    private Integer allOperations;

    private Integer successOperations;

    private Integer errorOperations;

    private String status;
}
