package com.gradle.ide.activities;
import android.view.MenuItem;
import android.view.Menu;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.gradle.ide.logger.Logger;
import java.util.Map;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.HashMap;
import com.gradle.ide.R;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialDialogs;
import android.app.Dialog;
import android.view.WindowManager;
import android.widget.GridLayout.LayoutParams;
import android.view.Window;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import com.gradle.ide.task.JDKInstallTask;
import com.gradle.ide.task.SDKInstallTask;
import com.gradle.ide.task.OtherInstallTask;
import com.gradle.ide.util.Utils;
import com.gradle.ide.util.FileUtil;
import java.io.File;
import org.json.JSONException;
import com.gradle.ide.util.Const;
import org.json.JSONObject;
import com.google.android.material.textfield.TextInputEditText;
import com.gradle.ide.model.Project;
import com.gradle.ide.logger.LogAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import com.gradle.ide.logger.LogViewModel;
import com.gradle.ide.logger.SystemLogPrinter;
import com.gradle.ide.build.GradleAsyncTask;
import com.google.android.material.textfield.TextInputLayout;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Objects;
import com.gradle.ide.task.ZipInstallTask;
import com.gradle.ide.build.task.GradleBundleReleaseTask;
import com.gradle.ide.build.task.GradleAssembleDebugTask;
import com.gradle.ide.build.GradleDebugTask;
import com.gradle.ide.build.GradleBundleTask;

public class MainActivity extends AppCompatActivity {
	private Logger mLogger;
	
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;

	private SharedPreferences pref;
	
	public interface ActivityResultCallback {
	    void onActivityResult(Intent intent);
	}

	private Map<Integer, ActivityResultCallback> callbackMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize(savedInstanceState);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		else {
			initializeLogic();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}

	private void initialize(Bundle savedInstanceState) {
		textInputLayout = findViewById(R.id.textInputLayout);
		textInputEditText = findViewById(R.id.textInputEditText);
		pref = getSharedPreferences("config", Activity.MODE_PRIVATE);
		
	}
	private void initializeLogic() {
		startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+getPackageName())));
		textInputLayout.setEndIconOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			i.addCategory(Intent.CATEGORY_DEFAULT);
			callbackMap.put(0, (data) -> {
			    Uri uri = data.getData(); 
    		    File file = new File(uri.getPath());
    		    final String[] split = file.getPath().split(":");
    		    textInputEditText.setText(FileUtil.getExternalStorageDir().concat("/").concat(split[1]));
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
				clipboard.setText(textInputEditText.getText());		
				});
			startActivityForResult(Intent.createChooser(i, "Choose directory"), 0);
		});

		textInputEditText.setText(pref.getString("textInputEditText", ""));
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
		    ActivityResultCallback cb = callbackMap.get(requestCode);
		    if (cb != null) {
		        cb.onActivityResult(data);
		    }
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
		if (id == R.id.action_gradle_build){		
			String[] option = {"gradle build", "gradle assembleDebug", "gradle bundleRelease"};		
			new MaterialAlertDialogBuilder(this)
				.setTitle("Select Build")
				.setItems(option, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {

							case 0: // gradle build

								mLogger = new Logger();
								mLogger.attach(MainActivity.this);
								Project p1 = new Project();

							
								p1.setLogger(mLogger); 
								LogAdapter logAdapter = new LogAdapter();
								final View buildDialog = getLayoutInflater().inflate(R.layout.build_dialog,null);
								final RecyclerView recyclerView =(RecyclerView)buildDialog.findViewById(R.id.recyclerview1);
								recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this)); 
								recyclerView.setAdapter(logAdapter);


								LogViewModel model = new ViewModelProvider(MainActivity.this).get(LogViewModel.class);
								model.getLogs().observe(MainActivity.this, (data) -> {
									logAdapter.submitList(data);
									recyclerView.smoothScrollToPosition(data.size() - 1);
								});

								MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(MainActivity.this);
								md.setTitle("Building...");
								md.setView(buildDialog);
								md.setCancelable(false);	
								md.setNegativeButton("Close", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();	
										}				
									});
								md.show();

								//textInputEditText.setText(pref.getString("textInputEditText", ""));


								//	textInputEditText.setText(pref.getString("textInputEditText", ""));


								SystemLogPrinter.start(mLogger);

								//File mProject = new File(textInputEditText.getText().toString());
								//Prefs.putString("project", mProject.getAbsolutePath());




								GradleAsyncTask task = new GradleAsyncTask(MainActivity.this);

								task.execute(p1);
								

								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;
							case 1: // gradle assembleDebug
								mLogger = new Logger();
								mLogger.attach(MainActivity.this);
								Project p2 = new Project();


								p2.setLogger(mLogger); 
								LogAdapter logAdapter2 = new LogAdapter();
								final View buildDialog2 = getLayoutInflater().inflate(R.layout.build_dialog,null);
								final RecyclerView recyclerView2 =(RecyclerView)buildDialog2.findViewById(R.id.recyclerview1);
								recyclerView2.setLayoutManager(new LinearLayoutManager(MainActivity.this)); 
								recyclerView2.setAdapter(logAdapter2);


								LogViewModel model2 = new ViewModelProvider(MainActivity.this).get(LogViewModel.class);
								model2.getLogs().observe(MainActivity.this, (data) -> {
									logAdapter2.submitList(data);
									recyclerView2.smoothScrollToPosition(data.size() - 1);
								});

								MaterialAlertDialogBuilder md2 = new MaterialAlertDialogBuilder(MainActivity.this);
								md2.setTitle("Building...");
								md2.setView(buildDialog2);
								md2.setCancelable(false);	
								md2.setNegativeButton("Close", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();	
										}				
									});
								md2.show();

								//textInputEditText.setText(pref.getString("textInputEditText", ""));


								//	textInputEditText.setText(pref.getString("textInputEditText", ""));


								SystemLogPrinter.start(mLogger);

								//File mProject = new File(textInputEditText.getText().toString());
								//Prefs.putString("project", mProject.getAbsolutePath());




								GradleDebugTask task2 = new GradleDebugTask(MainActivity.this);

								task2.execute(p2);
								

								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;	
							case 2: // gradle assembleDebug
								mLogger = new Logger();
								mLogger.attach(MainActivity.this);
								Project p3 = new Project();


								p3.setLogger(mLogger); 
								LogAdapter logAdapter3 = new LogAdapter();
								final View buildDialog3 = getLayoutInflater().inflate(R.layout.build_dialog,null);
								final RecyclerView recyclerView3 =(RecyclerView)buildDialog3.findViewById(R.id.recyclerview1);
								recyclerView3.setLayoutManager(new LinearLayoutManager(MainActivity.this)); 
								recyclerView3.setAdapter(logAdapter3);


								LogViewModel model3 = new ViewModelProvider(MainActivity.this).get(LogViewModel.class);
								model3.getLogs().observe(MainActivity.this, (data) -> {
									logAdapter3.submitList(data);
									recyclerView3.smoothScrollToPosition(data.size() - 1);
								});

								MaterialAlertDialogBuilder md3 = new MaterialAlertDialogBuilder(MainActivity.this);
								md3.setTitle("Building...");
								md3.setView(buildDialog3);
								md3.setCancelable(false);	
								md3.setNegativeButton("Close", new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();	
										}				
									});
								md3.show();

								//textInputEditText.setText(pref.getString("textInputEditText", ""));


								//	textInputEditText.setText(pref.getString("textInputEditText", ""));


								SystemLogPrinter.start(mLogger);

								//File mProject = new File(textInputEditText.getText().toString());
								//Prefs.putString("project", mProject.getAbsolutePath());




								GradleBundleTask task3 = new GradleBundleTask(MainActivity.this);

								task3.execute(p3);


								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;		
								
								
						}		}
				})
				.show();		
			return (true);					
		}
		if(id == R.id.action_create) {
			String[] option = {"Android Project", "Kotlin Project"};	
			new MaterialAlertDialogBuilder(this)
				.setTitle("Select Project")
				.setItems(option, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0: // Aapt
							
							final View projectDialog = getLayoutInflater().inflate(R.layout.project_dialog, null);
							final TextInputEditText edittext = (TextInputEditText)projectDialog.findViewById(R.id.projectEditText);
							final TextInputEditText edittext2 = (TextInputEditText)projectDialog.findViewById(R.id.packageEditText);
								MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(MainActivity.this);
								md.setTitle("Android Project");
								md.setView(projectDialog);
								md.setCancelable(false);	
								md.setPositiveButton("Create", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface p1, int p2) {
											String name = edittext.getText().toString().trim();
											String pack = edittext2.getText().toString().trim();
											createProj(name, pack);
										}
									});
								md.setNegativeButton("Cancel", null);
								md.create().show();
								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;
							case 1: // A
								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;	
						}		}
				})
				.show();		
			return (true);					
		}
		try {
		} catch (ActivityNotFoundException e) {
		}
		if(id == R.id.action_buildtools) {	

			String[] buildTools = {"Install JDK", "Install SDK", "Install Zip", "Other" };	
			new MaterialAlertDialogBuilder(this)
				.setTitle("Select Files To Be Installed")
				.setItems(buildTools, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0: // Aapt

								JDKInstallTask JdkTask = new JDKInstallTask(MainActivity.this, mLogger);
								Intent jdk = new Intent(Intent.ACTION_OPEN_DOCUMENT);
								jdk.setType("*/*");
								callbackMap.put(206, (data) -> {
									JdkTask.execute(data.getData());
								});

								startActivityForResult(Intent.createChooser(jdk, "Choose JDK"), 206);

								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;

							case 1: // A

								SDKInstallTask SdkTask = new SDKInstallTask(MainActivity.this, mLogger);
								Intent sdk = new Intent(Intent.ACTION_OPEN_DOCUMENT);			
								sdk.setType("*/*");
								callbackMap.put(207, (data) -> {
									SdkTask.execute(data.getData());
								});

								startActivityForResult(Intent.createChooser(sdk, "Choose JDK"), 207);
								

								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;	

							case 2: // A
								ZipInstallTask gradleWrapperTask = new ZipInstallTask(MainActivity.this);
								Intent gradleWrapper = new Intent(Intent.ACTION_OPEN_DOCUMENT);
								gradleWrapper.setType("*/*");
								callbackMap.put(208, (data) -> {
									gradleWrapperTask.execute(data.getData());
								});
								startActivityForResult(Intent.createChooser(gradleWrapper, "Choose Gradle Wrapper"), 208);
				
						try	{
								} catch (ActivityNotFoundException e) {
								}
								break;	
							case 3: // A
								OtherInstallTask OtherTask = new OtherInstallTask(MainActivity.this, mLogger);
								Intent other = new Intent(Intent.ACTION_OPEN_DOCUMENT);					
								other.setType("*/*");
								callbackMap.put(209, (data) -> {
									OtherTask.execute(data.getData());
								});

								startActivityForResult(Intent.createChooser(other, "Choose JDK"), 209);

								try	{
								} catch (ActivityNotFoundException e) {
								}
								break;	
						}	}

				})
				.show();		
			return (true);					
		}
		try {
		} catch (ActivityNotFoundException e) {
		}

		if(id == R.id.action_exit) {
			finish();   		
		}
		try {
		} catch (ActivityNotFoundException e) {
		}
		
		return super.onOptionsItemSelected(item);
	}
	@Override
    public void onBackPressed(){
	//AlertDialog dialog;
	//	AlertDialog alertDialog =  new MaterialAlertDialogBuilder(MainActivity.this)  // for fragment you can use getActivity() instead of this 
			//.setView(R.layout.activity_main) // custom layout is here 
			//.show();
			//alertDialog.dismiss();
		MaterialAlertDialogBuilder md = new MaterialAlertDialogBuilder(this);
		md.setTitle("Confirmation");
		md.setMessage("Do you want to exit this app?");
		md.setCancelable(false);
		md.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface p1, int p2)
                {
                    MainActivity.super.onBackPressed();
                    finish();
                }
            });
		md.setNegativeButton("No", null);
		md.show();
			}
	@Override
	public void onDestroy() {
		SharedPreferences.Editor editor = pref.edit();
		//fixme: why is there Objects.requireNonNull here?
	 	editor.putString("textInputEditText", Objects.requireNonNull(textInputEditText.getText()).toString());
		editor.commit();
		super.onDestroy();

	}
	private void createProj(String n, String p) {
        final String name = n;
        final String pack = p;
        if (name.isEmpty()){
            Utils.toast(getApplicationContext(), "Project name is Empty");
        } else if (pack.isEmpty()){
            Utils.toast(getApplicationContext(), "Package name is Empty");
        } else if (!pack.contains(".")){
            Utils.toast(getApplicationContext(), "Couldn't create the Project");
        } else if (FileUtil.isExistFile(Const.PROJECT_DIR.getAbsolutePath() + "/" + name)){
            Utils.toast(getApplicationContext(), "Project is already Exists" );
        } else {
            // make res path
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/lib");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/assets/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable-xhdpi/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/layout");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values");
            // make java path
            String package_path = pack.replace(".", "/") + File.separator;
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path);
            // copy res icons
         //   Utils.copyResources(R.drawable.app_icon, Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable-xhdpi/" + "app_icon.png");
            // write files
            FileUtil.writeFile2(Utils.readAssest("templates/buildG2.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/build.gradle");
            FileUtil.writeFile2(Utils.readAssest("templates/settings.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/settings.gradle");
            FileUtil.writeFile2(Utils.readAssest("templates/proguard.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/proguard-rules.pro");
            FileUtil.writeFile2(Utils.readAssest("templates/buildG.txt").replace("$<YOUR APPLICATION ID>$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/build.gradle");
            // write xml files
            FileUtil.writeFile2(Utils.readAssest("templates/AndroidManifest.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/AndroidManifest.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/activity_main.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/layout/activity_main.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/styles.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/styles.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/colors.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/colors.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/strings.txt").replace("$nam$", name), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/strings.xml");
            // write java files
            FileUtil.writeFile2(Utils.readAssest("templates/App.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "App.java");
            FileUtil.writeFile2(Utils.readAssest("templates/DebugActivity.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "DebugActivity.java");
            FileUtil.writeFile2(Utils.readAssest("templates/MainActivity.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "MainActivity.java");
            // Message
            Utils.toast(getApplicationContext(), "Project Created");
            try {
                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("packageName", pack);
                json.put("versionName", "1.0");
                json.put("versionCode", "1");
                json.put("minSdkVersion", "23");
                json.put("targetSdkVersion", "30");
                json.put("debug", "false");
                json.put("minify", "false");
                json.put("java8", "false");
                FileUtil.writeFile(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/setting", json.toString(4));
                FileUtil.writeFile(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/libs", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
	}
