package com.gradle.ide.task;

import java.lang.ref.WeakReference;
import android.content.Context;
import android.os.AsyncTask;
import android.net.Uri;
import org.apache.commons.compress.compressors.xz.*;
import org.apache.commons.compress.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import android.app.ProgressDialog;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import com.gradle.ide.logger.Logger;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.Dialog;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.view.LayoutInflater;
import android.app.Activity;
import com.gradle.ide.R;
import android.app.AlertDialog;
import android.widget.TextView;
import static com.gradle.ide.service.ApplicationLoader.getContext;
public class JDKInstallTask extends AsyncTask<Uri, String, String> {

	private WeakReference<Context> ref;
	private Logger mLogger;
	Activity activity;
	ProgressDialog progressDialog;
	TextView textView;
	
	public JDKInstallTask(Context context, Logger logger) {
		ref = new WeakReference<>(context);
		mLogger = logger;
	}

	@Override
	public void onPreExecute() {
		final View extractDialog = LayoutInflater.from(ref.get()).inflate(R.layout.extract_dialog, null, false);
		textView = (TextView) extractDialog.findViewById(R.id.textView);
		progressDialog = new ProgressDialog(ref.get());
		progressDialog.show();
		progressDialog.setCancelable(false);
		progressDialog.setContentView(extractDialog);
		progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


	}

	@Override
	public String doInBackground(Uri... params) {
		File dest = getContext().getFilesDir();
		Uri uri = params[0];

		String name = null;
		try {
			TarArchiveInputStream tarIn = null;

			tarIn = new TarArchiveInputStream(new XZCompressorInputStream(new BufferedInputStream(getContext().getContentResolver().openInputStream(uri))));

			TarArchiveEntry tarEntry = tarIn.getNextTarEntry();

			name = tarEntry.getName();

			while (tarEntry != null) {// create a file with the same name as the tarEntry
				File destPath = new File(dest, tarEntry.getName());
				System.out.println("Working: " + destPath.getCanonicalPath());
				if (tarEntry.isDirectory()) {
					destPath.mkdirs();
				} else {
					destPath.createNewFile();

					publishProgress("Extracting " + destPath.getName());
					//byte [] btoRead = new byte[(int)tarEntry.getSize()];
					byte [] btoRead = new byte[1024];
					//FileInputStream fin 
					//  = new FileInputStream(destPath.getCanonicalPath());
					BufferedOutputStream bout = 
						new BufferedOutputStream(new FileOutputStream(destPath));
					int len = 0;

					while((len = tarIn.read(btoRead)) != -1)
					{
						bout.write(btoRead,0,len);
					}

					bout.close();
					btoRead = null;

				}
				tarEntry = tarIn.getNextTarEntry();
			}
			tarIn.close();

			File renameFile = new File(getContext().getFilesDir(), "openjdk-17");
			renameFile.mkdirs();

			new File(getContext().getFilesDir(), name).renameTo(renameFile);

			setAllExecutable(renameFile.getParentFile());

		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}

		return "Extract Successful";
	}

	@Override
	public void onProgressUpdate(String... update) {
		textView.setText(update[0]);
	}

	@Override
	public void onPostExecute(String string) {
		progressDialog.dismiss();
	}

	private void setAllExecutable(File root) {
		File[] childs = root.listFiles();
		if (!root.setExecutable(true, false)) {
			e("setExecutable", "Failed to set as executable: " + root.getName());
		}
		if (childs != null) {
			for (File child: childs) {
				if (child.isDirectory()) {
					setAllExecutable(child);
				} else {
				    if (!child.setExecutable(true, false)) {
				        e("setExecutable", "Failed to set as executable: " + child.getName());
				    }
				}	    
			}
		}
	}

	void e(String TAG, String message) {
	    if (mLogger != null) {
	        mLogger.e(TAG, message);
	    }
	}
}
