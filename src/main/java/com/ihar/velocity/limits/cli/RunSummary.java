package com.ihar.velocity.limits.cli;

public final class RunSummary {

	private long accepted;
	private long declined;
	private long duplicates;
	private long invalid;

	void countDecided(boolean wasAccepted) {
		if (wasAccepted) {
			accepted++;
		} else {
			declined++;
		}
	}

	void countDuplicate() {
		duplicates++;
	}

	void countInvalid() {
		invalid++;
	}

	public long accepted() {
		return accepted;
	}

	public long declined() {
		return declined;
	}

	public long duplicates() {
		return duplicates;
	}

	public long invalid() {
		return invalid;
	}

	public long processed() {
		return accepted + declined + duplicates + invalid;
	}

	public long written() {
		return accepted + declined;
	}
}
