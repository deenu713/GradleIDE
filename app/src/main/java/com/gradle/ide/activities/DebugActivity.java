package com.gradle.ide.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import java.io.InputStream;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.ActivityNotFoundException;

public class DebugActivity extends Activity {

	String[] exceptionType = {
		"StringIndexOutOfBoundsException",
		"IndexOutOfBoundsException",
		"ArithmeticException",
		"NumberFormatException",
		"ActivityNotFoundException"

	};

	String[] errMessage= {
		"Invalid string operation\n",
		"Invalid list operation\n",
		"Invalid arithmetical operation\n",
		"Invalid toNumber block operation\n",
		"Invalid intent operation"
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String errMsg = "";
		String madeErrMsg = "";
		if(intent != null){
			errMsg = intent.getStringExtra("error");
			String[] spilt = errMsg.split("\n");
			//errMsg = spilt[0];
			try {
				for (int j = 0; j < exceptionType.length; j++) {
					if (spilt[0].contains(exceptionType[j])) {
						madeErrMsg = errMessage[j];
						int addIndex = spilt[0].indexOf(exceptionType[j]) + exceptionType[j].length();
						madeErrMsg += spilt[0].substring(addIndex, spilt[0].length());
						break;
					}
				}
				if(madeErrMsg.isEmpty()) madeErrMsg = errMsg;
			}catch(Exception e){}
		}
		MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(this);
		md.setTitle("An error occured");
		md.setCancelable(false);	
		md.setMessage(madeErrMsg);
		md.setNegativeButton("End Application", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					try	{
					} catch (ActivityNotFoundException e) {
					}	
				}
			});
		md.show();
		

    }
}
