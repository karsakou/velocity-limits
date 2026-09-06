package com.ihar.velocity.limits.core.rules;

import java.util.Optional;

import com.ihar.velocity.limits.config.VelocityLimitsProperties;
import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import com.ihar.velocity.limits.core.dto.ViolationDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public final class DailyCountRule implements VelocityRule {

	private final int limit;

	public DailyCountRule(VelocityLimitsProperties properties) {
		this.limit = properties.dailyCountLimit();
	}

	@Override
	public Optional<ViolationDto> validate(LoadAttemptDto attempt, CustomerActivityDto activity) {
		int count = activity.dayCount() + 1;
		if (count > limit) {
			return Optional.of(new ViolationDto(ViolationCode.DAILY_COUNT_LIMIT, "daily count " + count + " exceeds limit " + limit));
		}
		return Optional.empty();
	}
}
