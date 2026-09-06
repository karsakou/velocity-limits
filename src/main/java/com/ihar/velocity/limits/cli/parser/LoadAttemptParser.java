package com.ihar.velocity.limits.cli.parser;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class LoadAttemptParser {

	private static final Pattern AMOUNT = Pattern.compile("\\$(\\d+(?:\\.\\d{1,2})?)");
	private static final int MAX_TEXT_LENGTH = 64;
	private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

	private final JsonMapper mapper;

	public LoadAttemptParser(JsonMapper mapper) {
		this.mapper = mapper;
	}

	public LoadAttemptDto parse(String line) {
		LoadAttemptLineDto dto;
		try {
			dto = mapper.readValue(line, LoadAttemptLineDto.class);
		} catch (JacksonException e) {
			throw new InvalidLineException("malformed JSON: " + e.getOriginalMessage(), e);
		}
		if (dto == null) {
			throw new InvalidLineException("line is not a JSON object");
		}
		return new LoadAttemptDto(
				requireText(dto.id(), "id"),
				requireText(dto.customerId(), "customer_id"),
				parseAmount(dto.loadAmount()),
				parseTime(dto.time()));
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new InvalidLineException(field + " is required");
		}
		if (value.length() > MAX_TEXT_LENGTH) {
			throw new InvalidLineException(field + " is longer than " + MAX_TEXT_LENGTH + " characters");
		}
		return value;
	}

	private static BigDecimal parseAmount(String text) {
		if (text == null) {
			throw new InvalidLineException("load_amount is required");
		}
		var matcher = AMOUNT.matcher(text);
		if (!matcher.matches()) {
			throw new InvalidLineException("load_amount must be a dollar amount like \"$123.45\": " + text);
		}
		BigDecimal value = new BigDecimal(matcher.group(1));
		if (value.signum() <= 0) {
			throw new InvalidLineException("load_amount must be positive: " + text);
		}
		if (value.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidLineException("load_amount is larger than " + MAX_AMOUNT + ": " + text);
		}
		return value.setScale(2);
	}

	private static Instant parseTime(String text) {
		if (text == null) {
			throw new InvalidLineException("time is required");
		}
		try {
			return Instant.parse(text);
		} catch (DateTimeParseException e) {
			throw new InvalidLineException("time is not an ISO-8601 instant: " + text, e);
		}
	}
}
