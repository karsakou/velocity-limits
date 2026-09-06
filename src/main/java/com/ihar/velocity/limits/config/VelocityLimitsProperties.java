package com.ihar.velocity.limits.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "velocity.limits")
public record VelocityLimitsProperties(BigDecimal dailyAmount, BigDecimal weeklyAmount, int dailyCount) {

	public BigDecimal dailyAmountLimit() {
		return amount(dailyAmount, "velocity.limits.daily-amount");
	}

	public BigDecimal weeklyAmountLimit() {
		return amount(weeklyAmount, "velocity.limits.weekly-amount");
	}

	public int dailyCountLimit() {
		if (dailyCount <= 0) {
			throw new IllegalStateException("velocity.limits.daily-count must be positive, was " + dailyCount);
		}
		return dailyCount;
	}

	private static BigDecimal amount(BigDecimal value, String property) {
		if (value == null || value.signum() <= 0) {
			throw new IllegalStateException(property + " must be a positive amount, was " + value);
		}
		return value.setScale(2, RoundingMode.UNNECESSARY);
	}
}
