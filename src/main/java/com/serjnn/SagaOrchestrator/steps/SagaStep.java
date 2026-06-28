package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;
import com.serjnn.SagaOrchestrator.dto.SagaStepResult;

public interface SagaStep {

    SagaStepResult process(OrderDTO orderDTO);

    SagaStepResult revert(OrderDTO orderDTO);

}
