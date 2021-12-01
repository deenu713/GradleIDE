package com.gradle.ide.service;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.content.Intent;
import android.content.Context;
import com.gradle.ide.R;
import androidx.core.app.NotificationCompat;
import com.gradle.ide.activities.MainActivity;
import android.os.Message;

public class GradleService extends Service {

    private static final int NOTIFICATION_ID = 1;

    private HandlerThread handlerThread = new HandlerThread("GradleThread");
    private Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().contains("start")) {
            startForeground(NOTIFICATION_ID, getNotification());

            compile(intent);
        }
        return START_STICKY;
    }

    private Notification getNotification() {
        Context context = getApplicationContext();

        PendingIntent action = PendingIntent.getActivity(context,					
		0, new Intent(context, MainActivity.class),				
		PendingIntent.FLAG_UPDATE_CURRENT); // Flag indicating that if the described PendingIntent already exists, the current one should be canceled before generating a new one.

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = "ide";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "GradleIDE",							
			NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification that indicates compilation progress.");
            manager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        return builder.setContentIntent(action)
			.setContentTitle("GradleIDE")
			// .setTicker(info)
			.setContentText("Preparing")
			//.setSmallIcon(R.drawable.ic_launcher)
			 .setContentIntent(action)
			.setOngoing(true).build();
    }

    private void updateNotification(String message) {

    }

    private void compile(Intent intent) {
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
			 @Override
			 public void handleMessage(Message message) {

			 }
        };

        mHandler.post(() -> {

        });
    }
}
