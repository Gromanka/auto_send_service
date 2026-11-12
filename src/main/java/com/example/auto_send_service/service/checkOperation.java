package com.example.auto_send_service.service;

import com.example.auto_send_service.dto.SlkResult;
import com.example.auto_send_service.dto.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class checkOperation {

    private final RestClient restClient;
    @Value("${slkService.url}")
    private String slkService;

    @Value("${validationService.url}")
    private String validationService;

    public SlkResult checkOperationSlk(Long id) {
        return restClient.post()
                .uri(slkService+"/CheckOperation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .body(Collections.singletonMap("id", id))
                .retrieve()
                .body(SlkResult.class);
    }

    public ValidationResult  checkOperationValidation(Long id) {
        return restClient.get()
                .uri(validationService+"/validation/{id}", id)
                .accept(MediaType.ALL)
                .retrieve()
                .body(ValidationResult.class);
    }
}