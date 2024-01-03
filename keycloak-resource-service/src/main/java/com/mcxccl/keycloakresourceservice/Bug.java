package com.mcxccl.keycloakresourceservice;

public record Bug(Long id, String submitter, String headline, String description, String project, BugSeverity severity,
		BugState state) {

	public enum BugSeverity {
		LOW, MINOR, MAJOR, CRITICAL
	}

	public enum BugState {
		OPEN, CLOSED
	}

	public static Bug emptyBug(String submitter) {
		return new Bug(null, submitter, null, null, null, BugSeverity.LOW, BugState.OPEN);
	}
}