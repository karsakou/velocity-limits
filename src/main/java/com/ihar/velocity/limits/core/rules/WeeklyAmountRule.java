package com.ihar.velocity.limits.core.rules;

import java.math.BigDecimal;
import java.util.Optional;

import com.ihar.velocity.limits.config.VelocityLimitsProperties;
import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import com.ihar.velocity.limits.core.dto.ViolationDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public final class WeeklyAmountRule implements VelocityRule {

	private final BigDecimal limit;

	public WeeklyAmountRule(VelocityLimitsProperties properties) {
		this.limit = properties.weeklyAmountLimit();
	}

	@Override
	public Optional<ViolationDto> validate(LoadAttemptDto attempt, CustomerActivityDto activity) {
		BigDecimal total = activity.weekTotal().add(attempt.amount());
		if (total.compareTo(limit) > 0) {
			return Optional.of(new ViolationDto(ViolationCode.WEEKLY_AMOUNT_LIMIT, "weekly total " + total + " exceeds limit " + limit));
		}
		return Optional.empty();
	}
}
