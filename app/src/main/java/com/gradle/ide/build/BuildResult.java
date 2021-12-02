package com.gradle.ide.build;

public class BuildResult {

	private boolean isError;
	private String message;

	public BuildResult(String message, boolean error) {
		this.isError = error;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public boolean isError() {
		return isError;
	}
}
