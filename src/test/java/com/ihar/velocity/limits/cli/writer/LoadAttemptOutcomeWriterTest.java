package com.ihar.velocity.limits.cli.writer;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptOutcomeDto;
import com.ihar.velocity.limits.core.dto.ViolationCode;
import com.ihar.velocity.limits.core.dto.ViolationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class LoadAttemptOutcomeWriterTest {

	@TempDir
	Path dir;

	private static String expectedLine(String id, String customerId, boolean accepted) {
		return """
				{"id":"%s","customer_id":"%s","accepted":%s}""".formatted(id, customerId, accepted);
	}

	@Test
	void writesDecisionLine() throws IOException {
		LoadAttemptDto attempt = new LoadAttemptDto("1234", "1234", new BigDecimal("1.00"), Instant.parse("2000-01-01T00:00:00Z"));
		Path file = dir.resolve("out.txt");

		try (LoadAttemptOutcomeWriter writer = LoadAttemptOutcomeWriter.toFile(file, JsonMapper.builder().build())) {
			writer.write(new LoadAttemptOutcomeDto.Decided(attempt, List.of()));
			writer.write(new LoadAttemptOutcomeDto.Decided(attempt, List.of(new ViolationDto(ViolationCode.DAILY_AMOUNT_LIMIT, "x"))));
		}

		assertThat(Files.readAllLines(file)).containsExactly(
				expectedLine(attempt.loadId(), attempt.customerId(), true),
				expectedLine(attempt.loadId(), attempt.customerId(), false));
	}
}
