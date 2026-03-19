package com.serjnn.SagaOrchestrator.services;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.steps.SagaStep;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OrchService {

    private static final Logger log = LoggerFactory.getLogger(OrchService.class);

    private final List<SagaStep> steps;

    public OrchService(List<SagaStep> steps) {
        this.steps = steps;
    }


    public boolean start(OrderDTO orderDTO) {
        log.info("starting transaction");
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            for (SagaStep step : steps) {
                boolean success = step.process(orderDTO);
                if (success) {
                    completedSteps.add(step);
                } else {
                    revert(orderDTO, completedSteps);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error in process: {}, triggering rollback.", e.getMessage());
            revert(orderDTO, completedSteps);
            return false;
        }
    }

    private void revert(OrderDTO orderDTO, List<SagaStep> completedSteps) {
        log.info("Rolling back completed steps: {}", completedSteps.size());
        List<SagaStep> reverseSteps = new ArrayList<>(completedSteps);
        Collections.reverse(reverseSteps);
        reverseSteps.forEach(step -> step.revert(orderDTO));
    }
}
