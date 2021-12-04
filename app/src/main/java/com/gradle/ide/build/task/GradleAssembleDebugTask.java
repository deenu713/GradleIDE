package com.gradle.ide.build.task;
import com.gradle.ide.build.Launcher;
	import com.gradle.ide.build.exception.LauncherException;
	import java.io.IOException;
	import com.gradle.ide.build.java.JAVALauncher;
	import com.gradle.ide.model.Project;
	import java.util.Map;
	import java.util.HashMap;
	import java.util.List;
	import java.util.ArrayList;
	import java.io.InputStream;
	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import static com.gradle.ide.service.ApplicationLoader.getContext;
	import android.content.ClipboardManager;
	import java.io.File;
	import com.gradle.ide.util.Prefs;
	import android.os.Environment;
public class GradleAssembleDebugTask extends Launcher {
		private static final String TAG = "TASK";

		private Project mProject;
		private JAVALauncher JavaLauncher;

	public GradleAssembleDebugTask(Project project) {
			mProject = project;
		}

		@Override
		public void prepare() throws LauncherException {
			Map<String, String> env = new HashMap<>();

			env.put("HOME", getContext().getFilesDir().getAbsolutePath());
			env.put("JAVA_HOME", getContext().getFilesDir() + "/openjdk-17");
			env.put("ANDROID_SDK_ROOT", getContext().getFilesDir() + "/android-sdk");
			env.put("LD_LIBRARY_PATH", getContext().getFilesDir() + "/openjdk-17/lib:"
					+ getContext().getFilesDir() + "/openjdk-17/lib/jli:"
					+ getContext().getFilesDir() + "/openjdk-17/lib/server:"
					+ getContext().getFilesDir() + "/openjdk-17/lib/hm:");
			env.put("GRADLE_USER_HOME", getContext().getFilesDir() + "/.gradle");
			File tempDir = new File(getContext().getCacheDir(), "temp");
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			env.put("TMPDIR", tempDir.getAbsolutePath());

			JavaLauncher = new JAVALauncher(getContext());
			JavaLauncher.setEnvironment(env);
		} 
		@Override
		public void run() throws LauncherException, IOException {
			List<String> args = new ArrayList<>();
			ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE); 

			File mProject = new File(clipboard.getText().toString());
			Prefs.putString("mProject", mProject.getAbsolutePath());
			//args.add("-jar");
			//	args.add("/storage/emulated/0/Download/gradle-7.3.1/lib/gradle-launcher-7.3.1.jar");
			args.add("-Djava.io.tmpdir=" + getContext().getCacheDir().getAbsolutePath());
			args.add("-Xmx256m");
			args.add("-Xms256m");
			args.add("-Djava.awt.headless=true");
			args.add("-Dorg.gradle.appname=gradlew");
			args.add("-classpath");
			//	args.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AppProjects/Test/gradle/wrapper/gradle-wrapper.jar");
			args.add(getContext().getFilesDir().getAbsolutePath() + "/gradle/wrapper/gradle-wrapper.jar");
			//args.add("gradle/wrapper/gradle-wrapper.jar");
			args.add("org.gradle.wrapper.GradleWrapperMain"); 
			args.add("assembleDebug");
			try {
				Process process = JavaLauncher.launchJVM(args);
				loadStream(process.getInputStream(), false);
				loadStream(process.getErrorStream(), true);
				int rc = process.waitFor();

				if (rc != 0) {
					throw new LauncherException("Compilation failed, check output for more details.");
				}
			} catch (Exception e) {
				throw new LauncherException(e.getMessage());
			}
		}

		private void loadStream(InputStream s, boolean error) throws Exception {
			BufferedReader br = new BufferedReader(new InputStreamReader(s));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				if (error) {
					mProject.getLogger().e(TAG, line);
				} else {
					mProject.getLogger().d(TAG, line);
				}
			}
		}

		private static void addToEnvIfPresent(Map<String, String> map, String env) {
			String value = System.getenv(env);
			if (value != null) {
				map.put(env, value);
			}
		}
	}
