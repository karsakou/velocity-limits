package com.ihar.velocity.limits.cli.writer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "customer_id", "accepted" })
record LoadAttemptOutcomeLineDto(
		@JsonProperty("id") String id,
		@JsonProperty("customer_id") String customerId,
		@JsonProperty("accepted") boolean accepted) {
}
