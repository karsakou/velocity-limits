package com.ihar.velocity.limits;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.ihar.velocity.limits.cli.ProcessFileRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(args = { "src/test/resources/input.txt", "--output=build/golden-output.txt" })
class GoldenFileTest {

	@Autowired
	ProcessFileRunner runner;

	@Test
	void matchesExpectedOutput() throws IOException {
		List<String> expected = Files.readAllLines(Path.of("src/test/resources/output.txt"));
		List<String> actual = Files.readAllLines(Path.of("build/golden-output.txt"));

		assertThat(actual).containsExactlyElementsOf(expected);
		assertThat(runner.getExitCode()).isZero();
		assertThat(runner.summary().duplicates()).isEqualTo(1);
		assertThat(runner.summary().invalid()).isZero();
	}
}
