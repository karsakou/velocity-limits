package com.ihar.velocity.limits.core;

import com.ihar.velocity.limits.core.dto.ViolationCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
class LoadAttemptViolation {

	@Enumerated(EnumType.STRING)
	@Column(name = "code", nullable = false, length = 32)
	private ViolationCode code;

	@Column(name = "detail", nullable = false, length = 255)
	private String detail;

	protected LoadAttemptViolation() {
		// for JPA
	}

	LoadAttemptViolation(ViolationCode code, String detail) {
		this.code = code;
		this.detail = detail;
	}
}
