package com.ihar.velocity.limits.cli.parser;

public class InvalidLineException extends RuntimeException {

	public InvalidLineException(String message) {
		super(message);
	}

	public InvalidLineException(String message, Throwable cause) {
		super(message, cause);
	}
}
