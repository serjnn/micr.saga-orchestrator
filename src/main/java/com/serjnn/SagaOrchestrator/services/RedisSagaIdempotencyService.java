package com.serjnn.SagaOrchestrator.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedisSagaIdempotencyService implements SagaIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(RedisSagaIdempotencyService.class);
    private static final String SAGA_KEY_PREFIX = "saga:order:";

    private final StringRedisTemplate redisTemplate;

    public RedisSagaIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryStartSaga(UUID orderId, Duration lockTtl) {
        String key = SAGA_KEY_PREFIX + orderId;
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "IN_PROGRESS", lockTtl);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Redis unavailable while acquiring lock for order {}: {}. Allowing execution.", orderId, e.getMessage());
            return true;
        }
    }

    @Override
    public Optional<String> getSagaState(UUID orderId) {
        String key = SAGA_KEY_PREFIX + orderId;
        try {
            String state = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(state);
        } catch (Exception e) {
            log.warn("Redis unavailable while reading state for order {}: {}", orderId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void markCompleted(UUID orderId, Duration ttl) {
        String key = SAGA_KEY_PREFIX + orderId;
        try {
            redisTemplate.opsForValue().set(key, "COMPLETED", ttl);
            log.info("Marked saga {} as COMPLETED in Redis", orderId);
        } catch (Exception e) {
            log.warn("Failed to mark saga {} as COMPLETED in Redis: {}", orderId, e.getMessage());
        }
    }

    @Override
    public void markRolledBack(UUID orderId, Duration ttl) {
        String key = SAGA_KEY_PREFIX + orderId;
        try {
            redisTemplate.opsForValue().set(key, "ROLLED_BACK", ttl);
            log.info("Marked saga {} as ROLLED_BACK in Redis", orderId);
        } catch (Exception e) {
            log.warn("Failed to mark saga {} as ROLLED_BACK in Redis: {}", orderId, e.getMessage());
        }
    }
}
