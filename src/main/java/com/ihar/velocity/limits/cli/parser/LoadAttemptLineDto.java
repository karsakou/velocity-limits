package com.ihar.velocity.limits.cli.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

record LoadAttemptLineDto(
		@JsonProperty("id") String id,
		@JsonProperty("customer_id") String customerId,
		@JsonProperty("load_amount") String loadAmount,
		@JsonProperty("time") String time) {
}
