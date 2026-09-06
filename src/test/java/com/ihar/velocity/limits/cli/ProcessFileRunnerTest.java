package com.ihar.velocity.limits.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(args = { "src/test/resources/invalid-lines.txt", "--output=build/invalid-output.txt" })
class ProcessFileRunnerTest {

	@Autowired
	ProcessFileRunner runner;

	@Test
	void skipsInvalidLines() throws IOException {
		assertThat(Files.readAllLines(Path.of("build/invalid-output.txt"))).isEmpty();
		assertThat(runner.summary().invalid()).isEqualTo(10);
		assertThat(runner.summary().processed()).isEqualTo(10);
		assertThat(runner.getExitCode()).isEqualTo(ProcessFileRunner.EXIT_INVALID_INPUT);
	}
}
