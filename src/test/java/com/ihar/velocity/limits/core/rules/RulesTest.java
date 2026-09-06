package com.ihar.velocity.limits.core.rules;

import java.math.BigDecimal;
import java.time.Instant;

import com.ihar.velocity.limits.config.VelocityLimitsProperties;
import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RulesTest {

	private static final VelocityLimitsProperties LIMITS =
			new VelocityLimitsProperties(new BigDecimal("5000.00"), new BigDecimal("20000.00"), 3);

	private static BigDecimal bd(String plain) {
		return new BigDecimal(plain);
	}

	private static LoadAttemptDto attempt(String amount) {
		return new LoadAttemptDto("1", "c1", bd(amount), Instant.parse("2000-01-01T00:00:00Z"));
	}

	private static CustomerActivityDto activity(String dayTotal, int dayCount, String weekTotal) {
		return new CustomerActivityDto(bd(dayTotal), dayCount, bd(weekTotal));
	}

	@Test
	void dailyAmountLimit() {
		DailyAmountRule rule = new DailyAmountRule(LIMITS);
		assertThat(rule.validate(attempt("3000.00"), activity("2000.00", 1, "2000.00"))).isEmpty();
		assertThat(rule.validate(attempt("3000.01"), activity("2000.00", 1, "2000.00")))
				.hasValueSatisfying(v -> assertThat(v.code()).isEqualTo(ViolationCode.DAILY_AMOUNT_LIMIT));
		assertThat(rule.validate(attempt("5000.01"), CustomerActivityDto.NONE))
				.hasValueSatisfying(v -> assertThat(v.code()).isEqualTo(ViolationCode.DAILY_AMOUNT_LIMIT));
	}

	@Test
	void weeklyAmountLimit() {
		WeeklyAmountRule rule = new WeeklyAmountRule(LIMITS);
		assertThat(rule.validate(attempt("5000.00"), activity("0.00", 0, "15000.00"))).isEmpty();
		assertThat(rule.validate(attempt("5000.01"), activity("0.00", 0, "15000.00")))
				.hasValueSatisfying(v -> assertThat(v.code()).isEqualTo(ViolationCode.WEEKLY_AMOUNT_LIMIT));
	}

	@Test
	void dailyCountLimit() {
		DailyCountRule rule = new DailyCountRule(LIMITS);
		assertThat(rule.validate(attempt("1.00"), activity("2.00", 2, "2.00"))).isEmpty();
		assertThat(rule.validate(attempt("1.00"), activity("3.00", 3, "3.00")))
				.hasValueSatisfying(v -> assertThat(v.code()).isEqualTo(ViolationCode.DAILY_COUNT_LIMIT));
	}

	@Test
	void violationHasDetail() {
		DailyAmountRule rule = new DailyAmountRule(LIMITS);
		assertThat(rule.validate(attempt("5000.01"), CustomerActivityDto.NONE))
				.hasValueSatisfying(v -> assertThat(v.detail()).isEqualTo("daily total 5000.01 exceeds limit 5000.00"));
	}
}
