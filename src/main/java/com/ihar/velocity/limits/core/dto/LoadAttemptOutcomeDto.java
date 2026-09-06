package com.ihar.velocity.limits.core.dto;

import java.util.List;

public sealed interface LoadAttemptOutcomeDto {

	LoadAttemptDto attempt();

	record Decided(LoadAttemptDto attempt, List<ViolationDto> violations) implements LoadAttemptOutcomeDto {

		public boolean accepted() {
			return violations.isEmpty();
		}
	}

	record Duplicate(LoadAttemptDto attempt) implements LoadAttemptOutcomeDto {
	}
}
