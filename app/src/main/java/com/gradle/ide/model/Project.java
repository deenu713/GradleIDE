package com.gradle.ide.model;

import java.util.List;
import java.io.File;
import com.gradle.ide.logger.Logger;

public class Project {

	private Logger mLogger;
	private File mProjectDir;
	public Project() {
	}
	public File getProjectDir() {
		return mProjectDir;
	}
	public void setProjectDir(File file) {
		mProjectDir= file;
	}

	public Logger getLogger() {
		return mLogger;
	}

	public void setLogger(Logger logger) {
		mLogger = logger;
	}

}
