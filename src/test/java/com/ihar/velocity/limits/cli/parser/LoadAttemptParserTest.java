package com.ihar.velocity.limits.cli.parser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadAttemptParserTest {

	private final LoadAttemptParser parser = new LoadAttemptParser(JsonMapper.builder().build());

	private static String line(String id, String customer, String amount, String time) {
		return """
				{"id":"%s","customer_id":"%s","load_amount":"%s","time":"%s"}"""
				.formatted(id, customer, amount, time);
	}

	@Test
	void parsesValidLine() {
		LoadAttemptDto attempt = parser.parse(line("15887", "528", "$3318.47", "2000-01-01T00:00:00Z"));

		assertThat(attempt.loadId()).isEqualTo("15887");
		assertThat(attempt.customerId()).isEqualTo("528");
		assertThat(attempt.amount()).isEqualByComparingTo("3318.47");
		assertThat(attempt.time()).isEqualTo(Instant.parse("2000-01-01T00:00:00Z"));
	}

	@ParameterizedTest
	@CsvSource({ "$5000, 5000.00", "$0.5, 0.50", "$0.01, 0.01" })
	void normalisesAmount(String input, String expected) {
		BigDecimal amount = parser.parse(line("1", "1", input, "2000-01-01T00:00:00Z")).amount();
		assertThat(amount).isEqualByComparingTo(expected);
		assertThat(amount.scale()).isEqualTo(2);
	}

	@ParameterizedTest
	@ValueSource(strings = { "3000.00", "$", "$abc", "$1,000.00", "$10.123", "$-10.00", "$0.00", "USD 10", "",
			"$1e3", "$+10.00", "$10.", "$10000000000.00" })
	void rejectsBadAmount(String amount) {
		assertThatThrownBy(() -> parser.parse(line("1", "1", amount, "2000-01-01T00:00:00Z")))
				.isInstanceOf(InvalidLineException.class)
				.hasMessageContaining("load_amount");
	}

	@ParameterizedTest
	@ValueSource(strings = { "2000-01-01 00:00:00", "2000-01-01", "01/01/2000", "" })
	void rejectsBadTime(String time) {
		assertThatThrownBy(() -> parser.parse(line("1", "1", "$10.00", time)))
				.isInstanceOf(InvalidLineException.class)
				.hasMessageContaining("time");
	}

	private static Stream<String> malformedOrIncompleteLines() {
		return Stream.of(
				"this is not json",
				"""
				{"id":"5","customer_id":"1","time":"2000-01-01T00:00:00Z"}""",
				"""
				{"id":"6","load_amount":"$10.00","time":"2000-01-01T00:00:00Z"}""",
				"""
				{"customer_id":"1","load_amount":"$10.00","time":"2000-01-01T00:00:00Z"}""",
				line("", "1", "$10.00", "2000-01-01T00:00:00Z"),
				line("1", "  ", "$10.00", "2000-01-01T00:00:00Z"),
				line("x".repeat(65), "1", "$10.00", "2000-01-01T00:00:00Z"),
				line("1", "x".repeat(65), "$10.00", "2000-01-01T00:00:00Z"),
				"null");
	}

	@ParameterizedTest
	@MethodSource("malformedOrIncompleteLines")
	void rejectsBadLine(String text) {
		assertThatThrownBy(() -> parser.parse(text))
				.isInstanceOf(InvalidLineException.class)
				.hasMessageMatching(".+");
	}
}
