package com.ihar.velocity.limits.cli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(args = "/no/such/velocity-limits-input.txt")
class ProcessFileRunnerMissingInputTest {

	@Autowired
	ProcessFileRunner runner;

	@Test
	void missingFileFailsCleanly() {
		assertThat(runner.getExitCode()).isEqualTo(ProcessFileRunner.EXIT_IO_ERROR);
		assertThat(runner.summary().processed()).isZero();
	}
}
