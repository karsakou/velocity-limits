package com.ihar.velocity.limits.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.ihar.velocity.limits.cli.parser.InvalidLineException;
import com.ihar.velocity.limits.cli.parser.LoadAttemptParser;
import com.ihar.velocity.limits.cli.writer.LoadAttemptOutcomeWriter;
import com.ihar.velocity.limits.core.LoadAttemptService;
import com.ihar.velocity.limits.core.dto.LoadAttemptDto;
import com.ihar.velocity.limits.core.dto.LoadAttemptOutcomeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// Reads the input file line by line and writes one decision per line.
// Exit code: 0 ok, 1 some line invalid, 2 usage, 3 I/O error, 4 unexpected error.
@Component
public class ProcessFileRunner implements ApplicationRunner, ExitCodeGenerator {

	private static final Logger log = LoggerFactory.getLogger(ProcessFileRunner.class);

	static final int EXIT_OK = 0;
	static final int EXIT_INVALID_INPUT = 1;
	static final int EXIT_USAGE = 2;
	static final int EXIT_IO_ERROR = 3;
	static final int EXIT_UNEXPECTED_ERROR = 4;

	private final LoadAttemptService service;
	private final LoadAttemptParser parser;
	private final JsonMapper mapper;

	private int exitCode = EXIT_OK;
	private final RunSummary summary = new RunSummary();

	public ProcessFileRunner(LoadAttemptService service, LoadAttemptParser parser, JsonMapper mapper) {
		this.service = service;
		this.parser = parser;
		this.mapper = mapper;
	}

	@Override
	public void run(ApplicationArguments args) {
		List<String> files = args.getNonOptionArgs();
		if (files.size() != 1) {
			log.error("usage: java -jar velocity-limits.jar <input.txt> [--output=<file>]");
			exitCode = EXIT_USAGE;
			return;
		}
		Path input = Path.of(files.get(0));
		List<String> outputOption = args.getOptionValues("output");
		if (outputOption != null && (outputOption.isEmpty() || outputOption.get(0).isBlank())) {
			log.error("--output needs a file name: --output=<file>");
			exitCode = EXIT_USAGE;
			return;
		}
		Path output = outputOption == null ? null : Path.of(outputOption.get(0));
		String destination = output == null ? "standard output" : "file " + output;

		if (!Files.isReadable(input)) {
			log.error("input file not found or not readable: {}", input);
			exitCode = EXIT_IO_ERROR;
			return;
		}

		log.info("Processing load attempts from {}, writing decisions to {} ...", input, destination);
		try (BufferedReader in = Files.newBufferedReader(input, StandardCharsets.UTF_8);
				LoadAttemptOutcomeWriter writer = output == null
						? LoadAttemptOutcomeWriter.toStdout(mapper)
						: LoadAttemptOutcomeWriter.toFile(output, mapper)) {
			processLines(in, writer);
		}
		catch (IOException e) {
			log.error("I/O error while processing {}: {}", input, e.getMessage());
			exitCode = EXIT_IO_ERROR;
			return;
		}
		catch (RuntimeException e) {
			log.error("unexpected error, stopped after {} attempts; output is incomplete", summary.processed(), e);
			exitCode = EXIT_UNEXPECTED_ERROR;
			return;
		}
		log.info("Done: {} attempts processed, {} decisions written to {}",
				summary.processed(), summary.written(), destination);
		log.info("accepted={} declined={} duplicates={} invalid={}",
				summary.accepted(), summary.declined(), summary.duplicates(), summary.invalid());
		exitCode = summary.invalid() > 0 ? EXIT_INVALID_INPUT : EXIT_OK;
	}

	private void processLines(BufferedReader in, LoadAttemptOutcomeWriter writer) throws IOException {
		String line;
		long lineNumber = 0;
		while ((line = in.readLine()) != null) {
			lineNumber++;
			if (line.isBlank()) {
				continue;
			}
			MDC.put("line", Long.toString(lineNumber));
			try {
				processLine(line, writer);
			} catch (InvalidLineException e) {
				summary.countInvalid();
				log.warn("skipping invalid line: {}", e.getMessage());
			} finally {
				MDC.clear();
			}
		}
	}

	private void processLine(String line, LoadAttemptOutcomeWriter writer) throws IOException {
		LoadAttemptDto attempt = parser.parse(line);
		MDC.put("loadId", attempt.loadId());
		MDC.put("customerId", attempt.customerId());
		LoadAttemptOutcomeDto outcome = service.process(attempt);
		switch (outcome) {
			case LoadAttemptOutcomeDto.Decided decided -> {
				writer.write(decided);
				summary.countDecided(decided.accepted());
			}
			case LoadAttemptOutcomeDto.Duplicate duplicate -> {
				summary.countDuplicate();
				log.debug("duplicate load id, no response written");
			}
		}
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}

	public RunSummary summary() {
		return summary;
	}
}
