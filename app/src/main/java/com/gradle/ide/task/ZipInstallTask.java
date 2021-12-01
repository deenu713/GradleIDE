package com.gradle.ide.task;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gradle.ide.util.Decompress;
import java.lang.ref.WeakReference;
import com.gradle.ide.R;
import static com.gradle.ide.service.ApplicationLoader.getContext;
import android.app.Activity;
import com.gradle.ide.logger.Logger;

	public class ZipInstallTask extends AsyncTask<Uri, String, String> {

		private WeakReference<Context> ref;
	

	private Logger mLogger;
	Activity activity;
	ProgressDialog progressDialog;
	TextView textView;
		public ZipInstallTask(Context context) {
			ref = new WeakReference<>(context);
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

			try {
				Uri uri = params[0];
				Decompress.unzip(getContext().getContentResolver().openInputStream(uri), getContext().getFilesDir().getAbsolutePath());
			} catch (Exception e) {
				return "Error: " + e.getMessage();
			}
			return "Success";
		}

		@Override
		public void onProgressUpdate(String... param) {
			progressDialog.setMessage(param[0]);
		}

		@Override
		public void onPostExecute(String result) {
			progressDialog.dismiss();

			MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(ref.get());
			md.setTitle("Result");
			md.setCancelable(false);	
			md.setMessage(result);
			md.setNegativeButton("Close", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();	
					}				
				});
			
			md.show();
			}
	}   
    
