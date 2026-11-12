package com.example.auto_send_service.dto;

import lombok.Data;
import java.util.List;


@Data
public class ValidationResult {
    private String errorMessage;
    private List<String> errors;
    private  boolean valid;
    private int status;
}
