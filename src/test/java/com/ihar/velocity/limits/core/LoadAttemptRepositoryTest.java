package com.ihar.velocity.limits.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class LoadAttemptRepositoryTest {

	@Autowired
	LoadAttemptRepository repository;

	@PersistenceContext
	EntityManager entityManager;

	private void save(String id, String amount, String time, boolean accepted) {
		LoadAttemptDto a = new LoadAttemptDto(id, "c1", new BigDecimal(amount), Instant.parse(time));
		List<LoadAttemptViolation> violations = accepted
				? List.of()
				: List.of(new LoadAttemptViolation(ViolationCode.DAILY_AMOUNT_LIMIT, "daily total " + amount + " exceeds limit 5000.00"));
		repository.save(new LoadAttempt(
				a.loadId(), a.customerId(), a.amount(),
				LocalDateTime.ofInstant(a.time(), ZoneOffset.UTC),
				LoadAttemptService.loadDate(a.time()), LoadAttemptService.weekStart(a.time()),
				accepted, violations));
	}

	private long violationRows() {
		entityManager.flush();
		return ((Number) entityManager.createNativeQuery("select count(*) from load_attempt_violation").getSingleResult()).longValue();
	}

	@Test
	void activityCountsAcceptedOnly() {
		save("1", "3000.00", "2000-01-03T10:00:00Z", true);
		save("2", "4000.00", "2000-01-03T11:00:00Z", false);
		save("3", "1000.00", "2000-01-04T10:00:00Z", true);
		save("4", "500.00",  "2000-01-10T10:00:00Z", true);

		CustomerActivityDto monday = repository.findActivity("c1", LocalDate.of(2000, 1, 3), LocalDate.of(2000, 1, 3));

		assertThat(monday.dayTotal()).isEqualByComparingTo("3000.00");
		assertThat(monday.dayCount()).isEqualTo(1);
		assertThat(monday.weekTotal()).isEqualByComparingTo("4000.00");
	}

	@Test
	void activityZeroForNewCustomer() {
		CustomerActivityDto none = repository.findActivity("nobody", LocalDate.of(2000, 1, 3), LocalDate.of(2000, 1, 3));

		assertThat(none.dayTotal()).isEqualByComparingTo("0");
		assertThat(none.dayCount()).isZero();
		assertThat(none.weekTotal()).isEqualByComparingTo("0");
	}

	@Test
	void existsIncludesDeclined() {
		save("1", "9000.00", "2000-01-03T10:00:00Z", false);

		assertThat(repository.existsByCustomerIdAndLoadId("c1", "1")).isTrue();
		assertThat(repository.existsByCustomerIdAndLoadId("c1", "2")).isFalse();
		assertThat(repository.existsByCustomerIdAndLoadId("c2", "1")).isFalse();
	}

	@Test
	void rejectsDuplicateLoadId() {
		save("1", "10.00", "2000-01-03T10:00:00Z", true);

		assertThatThrownBy(() -> save("1", "20.00", "2000-01-03T11:00:00Z", true))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void storesViolationRows() {
		save("1", "9000.00", "2000-01-03T10:00:00Z", false);
		assertThat(violationRows()).isEqualTo(1);

		save("2", "10.00", "2000-01-03T11:00:00Z", true);
		assertThat(violationRows()).isEqualTo(1);
	}

	@Test
	void rejectsDuplicateCode() {
		LoadAttemptDto a = new LoadAttemptDto("1", "c1", new BigDecimal("9000.00"), Instant.parse("2000-01-03T10:00:00Z"));
		LoadAttempt entity = new LoadAttempt(
				a.loadId(), a.customerId(), a.amount(),
				LocalDateTime.ofInstant(a.time(), ZoneOffset.UTC),
				LoadAttemptService.loadDate(a.time()), LoadAttemptService.weekStart(a.time()), false,
				List.of(new LoadAttemptViolation(ViolationCode.DAILY_AMOUNT_LIMIT, "first"),
						new LoadAttemptViolation(ViolationCode.DAILY_AMOUNT_LIMIT, "second")));

		assertThatThrownBy(() -> repository.saveAndFlush(entity))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
