package com.serjnn.SagaOrchestrator.steps;

import com.serjnn.SagaOrchestrator.dto.OrderDTO;

public interface SagaStep {

    Boolean process(OrderDTO orderDTO);

    Boolean revert(OrderDTO orderDTO);

}
