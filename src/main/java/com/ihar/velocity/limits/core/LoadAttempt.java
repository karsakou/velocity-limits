package com.ihar.velocity.limits.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "load_attempt")
public class LoadAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "load_id", nullable = false)
	private String loadId;

	@Column(name = "customer_id", nullable = false)
	private String customerId;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "load_time", nullable = false)
	private LocalDateTime loadTime;

	@Column(name = "load_date", nullable = false)
	private LocalDate loadDate;

	@Column(name = "week_start", nullable = false)
	private LocalDate weekStart;

	@Column(name = "accepted", nullable = false)
	private boolean accepted;

	@ElementCollection
	@CollectionTable(name = "load_attempt_violation", joinColumns = @JoinColumn(name = "load_attempt_id"))
	private List<LoadAttemptViolation> violations = new ArrayList<>();

	protected LoadAttempt() {
		// for JPA
	}

	LoadAttempt(String loadId, String customerId, BigDecimal amount, LocalDateTime loadTime,
			LocalDate loadDate, LocalDate weekStart, boolean accepted, List<LoadAttemptViolation> violations) {
		this.loadId = loadId;
		this.customerId = customerId;
		this.amount = amount;
		this.loadTime = loadTime;
		this.loadDate = loadDate;
		this.weekStart = weekStart;
		this.accepted = accepted;
		this.violations = new ArrayList<>(violations);
	}
}
