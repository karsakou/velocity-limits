package com.ihar.velocity.limits.core.rules;

import java.util.Optional;

import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.ViolationDto;

public interface VelocityRule {

	Optional<ViolationDto> validate(LoadAttemptDto attempt, CustomerActivityDto activity);
}
