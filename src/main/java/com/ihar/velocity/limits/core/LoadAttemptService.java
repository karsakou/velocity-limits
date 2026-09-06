package com.ihar.velocity.limits.core;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptOutcomeDto;
import com.ihar.velocity.limits.core.dto.ViolationDto;
import com.ihar.velocity.limits.core.rules.VelocityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadAttemptService {

	private static final Logger log = LoggerFactory.getLogger(LoadAttemptService.class);

	private final LoadAttemptRepository repository;
	private final List<VelocityRule> rules;

	public LoadAttemptService(LoadAttemptRepository repository, List<VelocityRule> rules) {
		this.repository = repository;
		this.rules = List.copyOf(rules);
	}

	@Transactional
	public LoadAttemptOutcomeDto process(LoadAttemptDto attempt) {
		if (repository.existsByCustomerIdAndLoadId(attempt.customerId(), attempt.loadId())) {
			return new LoadAttemptOutcomeDto.Duplicate(attempt);
		}
		CustomerActivityDto activity = repository.findActivity(
				attempt.customerId(), loadDate(attempt.time()), weekStart(attempt.time()));
		List<ViolationDto> violations = rules.stream()
				.flatMap(rule -> rule.validate(attempt, activity).stream())
				.toList();
		LoadAttemptOutcomeDto.Decided decision = new LoadAttemptOutcomeDto.Decided(attempt, violations);
		repository.save(toEntity(decision));
		log.debug("amount={} activity={} accepted={} violations={}",
				attempt.amount(), activity, decision.accepted(), violations);
		return decision;
	}

	static LocalDate loadDate(Instant time) {
		return time.atZone(ZoneOffset.UTC).toLocalDate();
	}

	static LocalDate weekStart(Instant time) {
		return loadDate(time).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

	private static LoadAttempt toEntity(LoadAttemptOutcomeDto.Decided decision) {
		LoadAttemptDto attempt = decision.attempt();
		List<LoadAttemptViolation> violations = decision.violations().stream()
				.map(v -> new LoadAttemptViolation(v.code(), v.detail()))
				.toList();
		return new LoadAttempt(
				attempt.loadId(),
				attempt.customerId(),
				attempt.amount(),
				LocalDateTime.ofInstant(attempt.time(), ZoneOffset.UTC),
				loadDate(attempt.time()),
				weekStart(attempt.time()),
				decision.accepted(),
				violations);
	}
}
