package com.serjnn.SagaOrchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(properties = {
    "eureka.client.enabled=false"
})
class SagaOrchestratorApplicationTests {

	@MockBean
	private StringRedisTemplate redisTemplate;

	@Test
	void contextLoads() {
	}

}
