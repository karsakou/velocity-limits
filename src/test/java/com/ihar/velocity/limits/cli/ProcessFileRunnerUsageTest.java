package com.ihar.velocity.limits.cli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(args = { "src/test/resources/input.txt", "--output" })
class ProcessFileRunnerUsageTest {

	@Autowired
	ProcessFileRunner runner;

	@Test
	void outputOptionNeedsAValue() {
		assertThat(runner.getExitCode()).isEqualTo(ProcessFileRunner.EXIT_USAGE);
		assertThat(runner.summary().processed()).isZero();
	}
}
