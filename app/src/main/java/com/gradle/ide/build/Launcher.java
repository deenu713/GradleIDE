package com.gradle.ide.build;
import java.io.IOException;
import java.io.File;
import com.gradle.ide.service.ApplicationLoader;
import com.gradle.ide.build.exception.LauncherException;
import com.gradle.ide.util.Decompress;

public abstract class Launcher {

	public interface OnProgressUpdateListener {
		void onProgressUpdate(String... update);
	}

	protected OnProgressUpdateListener listener;

	public void setProgressListener(OnProgressUpdateListener listener) {
		this.listener = listener;
	}

	public void onProgressUpdate(String... update) {
		if (listener != null) {
			listener.onProgressUpdate(update);
		}
	}

	abstract public void prepare() throws LauncherException ;

	abstract public void run() throws LauncherException, IOException;
	
	
	}
	
