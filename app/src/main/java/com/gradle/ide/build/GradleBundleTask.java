package com.gradle.ide.build;

import android.os.AsyncTask;
import android.content.Context;
import android.app.Dialog;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.net.Uri;
import java.lang.ref.WeakReference;
import java.io.IOException;
import java.io.File;
import com.gradle.ide.model.Project;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gradle.ide.build.task.GradleBuildTask;
import android.content.DialogInterface;
import com.gradle.ide.build.task.GradleBundleReleaseTask;

public class GradleBundleTask extends AsyncTask<Project, String, BuildResult> {

	private final WeakReference<Context> mContext;

	private TextView progress;
	private Dialog dialog;
	private MaterialAlertDialogBuilder builder;


	public GradleBundleTask(Context context) {
		mContext = new WeakReference<>(context);
	}

	@Override
	public void onPreExecute() {
		Context context = mContext.get();
	}

	@Override
	public BuildResult doInBackground(Project... params) {

	    Project project = params[0];
	    try {	
			GradleBundleReleaseTask gradleBundleTask = new GradleBundleReleaseTask(project);
			gradleBundleTask.prepare();
			gradleBundleTask.run();

		} catch (Exception e) {
			return new BuildResult(android.util.Log.getStackTraceString(e), true);
		}
	    return new BuildResult("Success", false);
	}

	@Override
	public void onProgressUpdate(String... update) {
		progress.setText(update[0]);
	}

	@Override
	public void onPostExecute(BuildResult result) {

		if (result.isError()) {
			MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(mContext.get());
			md.setTitle("Building Failed");
			md.setCancelable(false);	
			md.setMessage(result.getMessage());
			md.setNegativeButton("Close", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();	
					}				
				});
			md.show();
		}
	}
}
