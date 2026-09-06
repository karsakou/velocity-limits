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
@Order(1)
public final class DailyAmountRule implements VelocityRule {

	private final BigDecimal limit;

	public DailyAmountRule(VelocityLimitsProperties properties) {
		this.limit = properties.dailyAmountLimit();
	}

	@Override
	public Optional<ViolationDto> validate(LoadAttemptDto attempt, CustomerActivityDto activity) {
		BigDecimal total = activity.dayTotal().add(attempt.amount());
		if (total.compareTo(limit) > 0) {
			return Optional.of(new ViolationDto(ViolationCode.DAILY_AMOUNT_LIMIT, "daily total " + total + " exceeds limit " + limit));
		}
		return Optional.empty();
	}
}
