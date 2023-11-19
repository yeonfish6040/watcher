package com.yeonfish.watcher;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yeonfish.watcher.services.WorkManagerService;
import com.yeonfish.watcher.util.sql.SQLQuery;
import com.yeonfish.watcher.util.sql.SQLResults;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scheduleJob();
    }

    public void scheduleJob(){
        WorkRequest wr =
                new PeriodicWorkRequest.Builder(WorkManagerService.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance().enqueue(wr);

//        int resultCode = jobScheduler.schedule(info);
//        if(resultCode == jobScheduler.RESULT_SUCCESS){
//            Log.d(TAG, "작업 성공");
//        }else{
//            Log.d(TAG, "작업 실패");
//        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();

                new Thread(() -> {
                    SQLQuery sqlQuery = new SQLQuery("lyj.kr", "3306", "android", "f60ed56a9c8275894022fe5a7a1625c33bdb55b729bb4e38962af4d1613eda25", "android");
                    if (!sqlQuery.cStatus()) {
                        toastOnThread(MainActivity.this, "Fail1", Toast.LENGTH_LONG);
                        return;
                    }
                    SQLResults results = null;
                    try {
                        sqlQuery.update("UPDATE `FCMToken` SET `Token`='"+token+"' WHERE `id`=1");
                    } catch (Exception e) {
                        toastOnThread(MainActivity.this, "Fail2", Toast.LENGTH_LONG);
                        throw new RuntimeException(e);
                    }
                }).start();

                Log.d("PushService", "FCM_token: " + token);
            }
        });
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
