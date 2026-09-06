package com.ihar.velocity.limits.core.dto;

import java.math.BigDecimal;

public record CustomerActivityDto(BigDecimal dayTotal, int dayCount, BigDecimal weekTotal) {

	public static final CustomerActivityDto NONE = new CustomerActivityDto(BigDecimal.ZERO, 0, BigDecimal.ZERO);
}
