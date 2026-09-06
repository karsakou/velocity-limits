package com.ihar.velocity.limits.core;

import java.time.LocalDate;

import com.ihar.velocity.limits.core.dto.CustomerActivityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoadAttemptRepository extends JpaRepository<LoadAttempt, Long> {

	boolean existsByCustomerIdAndLoadId(String customerId, String loadId);

	@Query("""
			select new com.ihar.velocity.limits.core.dto.CustomerActivityDto(
			           coalesce(sum(case when e.loadDate = :day then e.amount else 0 end), 0),
			           coalesce(cast(sum(case when e.loadDate = :day then 1 else 0 end) as integer), 0),
			           coalesce(sum(e.amount), 0))
			      from LoadAttempt e
			     where e.customerId = :customerId
			       and e.weekStart = :weekStart
			       and e.accepted = true
			""")
	CustomerActivityDto findActivity(String customerId, LocalDate day, LocalDate weekStart);
}
