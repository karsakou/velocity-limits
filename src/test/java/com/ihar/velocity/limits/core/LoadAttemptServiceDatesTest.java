package com.ihar.velocity.limits.core;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoadAttemptServiceDatesTest {

	@Test
	void loadDateIsUtc() {
		assertThat(LoadAttemptService.loadDate(Instant.parse("2000-01-01T23:59:59Z"))).isEqualTo(LocalDate.of(2000, 1, 1));
		assertThat(LoadAttemptService.loadDate(Instant.parse("2000-01-02T00:00:00Z"))).isEqualTo(LocalDate.of(2000, 1, 2));
	}

	@Test
	void weekStartsMonday() {
		assertThat(LoadAttemptService.weekStart(Instant.parse("2000-01-01T00:00:00Z"))).isEqualTo(LocalDate.of(1999, 12, 27));
		assertThat(LoadAttemptService.weekStart(Instant.parse("2000-01-02T23:59:59Z"))).isEqualTo(LocalDate.of(1999, 12, 27));
		assertThat(LoadAttemptService.weekStart(Instant.parse("2000-01-03T00:00:00Z"))).isEqualTo(LocalDate.of(2000, 1, 3));
		assertThat(LoadAttemptService.weekStart(Instant.parse("2018-12-31T12:00:00Z"))).isEqualTo(LocalDate.of(2018, 12, 31));
	}

	@Test
	void weekAcrossNewYear() {
		assertThat(LoadAttemptService.weekStart(Instant.parse("2021-01-01T10:00:00Z"))).isEqualTo(LocalDate.of(2020, 12, 28));
	}
}
