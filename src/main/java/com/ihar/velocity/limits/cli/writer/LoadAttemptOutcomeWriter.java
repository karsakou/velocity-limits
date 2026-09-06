package com.ihar.velocity.limits.cli.writer;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptOutcomeDto;
import tools.jackson.databind.json.JsonMapper;

public final class LoadAttemptOutcomeWriter implements AutoCloseable {

	private final BufferedWriter out;
	private final JsonMapper mapper;
	private final boolean closeUnderlying;

	private LoadAttemptOutcomeWriter(BufferedWriter out, JsonMapper mapper, boolean closeUnderlying) {
		this.out = out;
		this.mapper = mapper;
		this.closeUnderlying = closeUnderlying;
	}

	public static LoadAttemptOutcomeWriter toStdout(JsonMapper mapper) {
		var stdout = new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), StandardCharsets.UTF_8);
		return new LoadAttemptOutcomeWriter(new BufferedWriter(stdout), mapper, false);
	}

	public static LoadAttemptOutcomeWriter toFile(Path path, JsonMapper mapper) throws IOException {
		return new LoadAttemptOutcomeWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), mapper, true);
	}

	public void write(LoadAttemptOutcomeDto.Decided decision) throws IOException {
		LoadAttemptDto attempt = decision.attempt();
		LoadAttemptOutcomeLineDto line = new LoadAttemptOutcomeLineDto(attempt.loadId(), attempt.customerId(), decision.accepted());
		out.write(mapper.writeValueAsString(line));
		out.newLine();
	}

	@Override
	public void close() throws IOException {
		out.flush();
		if (closeUnderlying) {
			out.close(); // don't close System.out
		}
	}
}
