package com.ihar.velocity.limits.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ihar.velocity.limits.config.VelocityLimitsProperties;
import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptOutcomeDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import com.ihar.velocity.limits.core.dto.ViolationDto;
import com.ihar.velocity.limits.core.rules.DailyAmountRule;
import com.ihar.velocity.limits.core.rules.DailyCountRule;
import com.ihar.velocity.limits.core.rules.WeeklyAmountRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoadAttemptServiceTest {

	private static final VelocityLimitsProperties LIMITS =
			new VelocityLimitsProperties(new BigDecimal("5000.00"), new BigDecimal("20000.00"), 3);

	private final LoadAttemptRepository repository = mock(LoadAttemptRepository.class);
	private final LoadAttemptService service = new LoadAttemptService(repository, List.of(
			new DailyAmountRule(LIMITS),
			new WeeklyAmountRule(LIMITS),
			new DailyCountRule(LIMITS)));

	private static LoadAttemptDto attempt(String amount) {
		return new LoadAttemptDto("1", "c1", new BigDecimal(amount), Instant.parse("2000-01-01T10:00:00Z"));
	}

	private static CustomerActivityDto activity(String dayTotal, int dayCount, String weekTotal) {
		return new CustomerActivityDto(new BigDecimal(dayTotal), dayCount, new BigDecimal(weekTotal));
	}

	@Test
	void duplicateShortCircuits() {
		when(repository.existsByCustomerIdAndLoadId("c1", "1")).thenReturn(true);

		LoadAttemptOutcomeDto outcome = service.process(attempt("10.00"));

		assertThat(outcome).isInstanceOf(LoadAttemptOutcomeDto.Duplicate.class);
		verify(repository, never()).findActivity(anyString(), any(), any());
		verify(repository, never()).save(any());
	}

	@Test
	void acceptsWithinLimits() {
		when(repository.existsByCustomerIdAndLoadId("c1", "1")).thenReturn(false);
		when(repository.findActivity(anyString(), any(), any())).thenReturn(activity("0.00", 0, "0.00"));

		LoadAttemptOutcomeDto outcome = service.process(attempt("3000.00"));

		assertThat(outcome).isInstanceOfSatisfying(LoadAttemptOutcomeDto.Decided.class,
				d -> assertThat(d.accepted()).isTrue());
		verify(repository).save(any());
	}

	@Test
	void savesDeclined() {
		when(repository.existsByCustomerIdAndLoadId("c1", "1")).thenReturn(false);
		when(repository.findActivity(anyString(), any(), any())).thenReturn(activity("3000.00", 1, "3000.00"));

		LoadAttemptOutcomeDto outcome = service.process(attempt("3000.00"));

		assertThat(outcome).isInstanceOfSatisfying(LoadAttemptOutcomeDto.Decided.class,
				d -> assertThat(d.accepted()).isFalse());
		verify(repository).save(any());
	}

	@Test
	void collectsAllViolations() {
		when(repository.existsByCustomerIdAndLoadId("c1", "1")).thenReturn(false);

		when(repository.findActivity(anyString(), any(), any())).thenReturn(activity("4000.00", 3, "4000.00"));

		LoadAttemptOutcomeDto outcome = service.process(attempt("3000.00"));

		assertThat(outcome).isInstanceOfSatisfying(LoadAttemptOutcomeDto.Decided.class, d ->
				assertThat(d.violations()).extracting(ViolationDto::code)
						.containsExactly(ViolationCode.DAILY_AMOUNT_LIMIT, ViolationCode.DAILY_COUNT_LIMIT));
	}
}
