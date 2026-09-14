package com.serjnn.SagaOrchestrator.controller;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaResponseDTO;
import com.serjnn.SagaOrchestrator.services.OrchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("${api.base-path:/api/v1}")
public class OrchController {

    private final OrchService orchService;

    public OrchController(OrchService orchService) {
        this.orchService = orchService;
    }

    @PostMapping
    public ResponseEntity<SagaResponseDTO> start(@Valid @RequestBody OrderDTO orderDTO) {
        SagaResponseDTO response = orchService.processSaga(orderDTO);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else if ("IN_PROGRESS".equals(response.message())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SagaResponseDTO(orderDTO.orderId(), false, "Saga is already in progress for this order"));
        } else {
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }
}
