package com.ihar.velocity.limits.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ihar.velocity.limits.core.LoadAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(args = { "src/test/resources/input.txt", "--output=build/unexpected-output.txt" })
class ProcessFileRunnerUnexpectedErrorTest {

	@Autowired
	ProcessFileRunner runner;

	@Test
	void stopsWithItsOwnExitCode() throws IOException {
		assertThat(runner.getExitCode()).isEqualTo(ProcessFileRunner.EXIT_UNEXPECTED_ERROR);
		assertThat(runner.summary().processed()).isZero();
		assertThat(Files.readAllLines(Path.of("build/unexpected-output.txt"))).isEmpty();
	}

	@TestConfiguration
	static class FailingService {

		@Bean
		@Primary
		LoadAttemptService failingLoadAttemptService() {
			LoadAttemptService service = mock(LoadAttemptService.class);
			when(service.process(any())).thenThrow(new DataIntegrityViolationException("boom"));
			return service;
		}
	}
}
