package com.serjnn.SagaOrchestrator.config;

import com.serjnn.SagaOrchestrator.steps.SagaStep;
import java.util.List;

public record SagaDefinition(String name, List<SagaStep> steps) {}
