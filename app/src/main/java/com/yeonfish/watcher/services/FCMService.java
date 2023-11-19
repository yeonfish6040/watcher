package com.yeonfish.watcher.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yeonfish.watcher.R;
import com.yeonfish.watcher.util.sql.SQLQuery;
import com.yeonfish.watcher.util.sql.SQLResults;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("PushService", "onNewToken: " + token);
        new Thread(() -> {
            SQLQuery sqlQuery = new SQLQuery("lyj.kr", "3306", "android", "f60ed56a9c8275894022fe5a7a1625c33bdb55b729bb4e38962af4d1613eda25", "android");
            if (!sqlQuery.cStatus()) {
                toastOnThread(FCMService.this, "Fail1", Toast.LENGTH_LONG);
                return;
            }
            SQLResults results = null;
            try {
                sqlQuery.update("UPDATE `FCMToken` SET `Token`='"+token+"' WHERE `id`=1");
            } catch (Exception e) {
                toastOnThread(FCMService.this, "Fail2", Toast.LENGTH_LONG);
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        if (remoteMessage == null) return;

        if (data.get("event").equals("notification")) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            String CHANNEL_ID = !data.containsKey("channel_id") ? "Watcher": data.get("channel_id");
            String CHANNEL_NAME = !data.containsKey("channel_name") ? "Watcher": data.get("channel_name");

            NotificationCompat.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(getApplicationContext());
            }

            String title = null;
            String body = null;
            String url = null;
            try {
                JSONObject notificationData = new JSONObject(data.get("body"));
                title = notificationData.getString("title");
                body = notificationData.getString("text");
            } catch (JSONException e) { Log.e(e.getMessage(), e.toString()); }

            builder.setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true);

            Notification notification = builder.build();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(1, notification);
        }else if (data.get("event").equals("ringing")) {

        }
    }


    protected void toastOnThread(Context context, String val, int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, val, duration).show();
            }
        }, 0);
    }
}
