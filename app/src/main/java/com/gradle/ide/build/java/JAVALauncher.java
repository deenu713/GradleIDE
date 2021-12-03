package com.gradle.ide.build.java;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import android.os.Environment;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import android.content.SharedPreferences;
import android.app.Activity;
import android.preference.PreferenceManager;
import com.gradle.ide.model.Project;
import android.view.View;
import android.view.LayoutInflater;
import com.google.android.material.textfield.TextInputEditText;
import com.gradle.ide.R;
import android.widget.EditText;
import com.gradle.ide.util.Prefs;
import android.content.ClipboardManager;
import android.content.ClipData; 
public class JAVALauncher {
	
    private Context mContext;
    private ProcessBuilder pb;
    private Map<String, String> customEnv;

	private Project mProject;



    public JAVALauncher(Context context) {
        mContext = context.getApplicationContext();
    }

    public void setEnvironment(Map<String, String> map) {
        customEnv = map;
    }
	public JAVALauncher(Project project) {
        mProject = project;
    }
    public void prepare() {

        pb = new ProcessBuilder();
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("HOME", mContext.getFilesDir().getAbsolutePath());
		env.put("PATH", System.getenv("PATH"));
		env.put("LANG", "en_US.UTF-8");
		env.put("PWD", mContext.getFilesDir().getAbsolutePath());
		env.put("BOOTCLASSPATH", System.getenv("BOOTCLASSPATH"));
		env.put("ANDROID_ROOT", System.getenv("ANDROID_ROOT"));
		env.put("ANDROID_DATA", System.getenv("ANDROID_DATA"));
		env.put("EXTERNAL_STORAGE", System.getenv("EXTERNAL_STORAGE"));
		env.put("JAVA_HOME", mContext.getFilesDir() + "/openjdk-17");
     	env.put("ANDROID_SDK_ROOT", mContext.getFilesDir() + "/android-sdk");
    	addToEnvIfPresent(env, "ANDROID_ART_ROOT");
		addToEnvIfPresent(env, "DEX2OATBOOTCLASSPATH");
		addToEnvIfPresent(env, "ANDROID_I18N_ROOT");
		addToEnvIfPresent(env, "ANDROID_RUNTIME_ROOT");
		addToEnvIfPresent(env, "ANDROID_TZDATA_ROOT");
		File tempDir = new File(mContext.getCacheDir(), "temp");
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		env.put("TMPDIR", tempDir.getAbsolutePath());
	

		if (customEnv != null) {
		    env.putAll(customEnv);
		}
			
	/*	final View ji = LayoutInflater.from(mContext)
			.inflate(R.layout.activity_main, null, false);
		final EditText gtt = (EditText) ji.findViewById(R.id.textInputEditText);*/

		ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE); 
		File mProject = new File(clipboard.getText().toString());
		Prefs.putString("mProject", mProject.getAbsolutePath());
		pb.directory(mProject);		
		pb.redirectErrorStream(true);
    }

	
    public Process launchJVM(List<String> args) throws Exception {
        prepare();

        List<String> arguments = new ArrayList<>();
        arguments.add(mContext.getFilesDir() + "/openjdk-17/bin/java");
        arguments.addAll(args);
        pb.command(arguments);
        return pb.start();
    }

    private static void addToEnvIfPresent(Map<String, String> map, String env) {
		String value = System.getenv(env);
		if (value != null) {
			map.put(env, value);
		}
	}
}
