package com.serjnn.SagaOrchestrator.controller;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaResponseDTO;
import com.serjnn.SagaOrchestrator.services.OrchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path:/api/v1}")
public class OrchController {

    private final OrchService orchService;

    public OrchController(OrchService orchService) {
        this.orchService = orchService;
    }

    @PostMapping
    public ResponseEntity<SagaResponseDTO> start(@Valid @RequestBody OrderDTO orderDTO) {
        boolean success = orchService.start(orderDTO);
        if (success) {
            return ResponseEntity.ok(new SagaResponseDTO(orderDTO.orderId(), true, "Order saga completed successfully"));
        } else {
            return ResponseEntity.unprocessableEntity().body(
                    new SagaResponseDTO(orderDTO.orderId(), false, "Order saga failed and compensations were executed")
            );
        }
    }
}
