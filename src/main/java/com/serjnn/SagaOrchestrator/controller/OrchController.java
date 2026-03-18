package com.serjnn.SagaOrchestrator.controller;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.services.OrchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrchController {

    private final OrchService orchService;

    public OrchController(OrchService orchService) {
        this.orchService = orchService;
    }

    @PostMapping
    public boolean start(@RequestBody OrderDTO orderDTO) {
        return orchService.start(orderDTO);
    }
}
