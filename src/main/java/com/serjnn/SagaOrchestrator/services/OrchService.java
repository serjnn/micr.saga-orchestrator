package com.serjnn.SagaOrchestrator.services;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.steps.BucketStep;
import com.serjnn.SagaOrchestrator.steps.ClientBalanceStep;
import com.serjnn.SagaOrchestrator.steps.OrderStep;
import com.serjnn.SagaOrchestrator.steps.SagaStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OrchService {

    private static final Logger log = LoggerFactory.getLogger(OrchService.class);

    private final ClientBalanceStep clientBalanceStep;
    private final BucketStep bucketStep;
    private final OrderStep orderStep;

    public OrchService(ClientBalanceStep clientBalanceStep, BucketStep bucketStep, OrderStep orderStep) {
        this.clientBalanceStep = clientBalanceStep;
        this.bucketStep = bucketStep;
        this.orderStep = orderStep;
    }

    private List<SagaStep> getSteps() {
        return List.of(clientBalanceStep, bucketStep, orderStep);
    }

    public boolean start(OrderDTO orderDTO) {
        log.info("starting transaction");
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            for (SagaStep step : getSteps()) {
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
