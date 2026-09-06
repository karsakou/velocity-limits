package com.ihar.velocity.limits.core.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoadAttemptDto(String loadId, String customerId, BigDecimal amount, Instant time) {
}
