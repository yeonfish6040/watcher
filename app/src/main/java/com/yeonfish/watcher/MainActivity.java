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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yeonfish.watcher.services.FCMService;
import com.yeonfish.watcher.services.JobService;
import com.yeonfish.watcher.util.sql.SQLQuery;
import com.yeonfish.watcher.util.sql.SQLResults;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scheduleJob();
    }

    public void scheduleJob(){
        ComponentName componentName = new ComponentName(this, JobService.class);

        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)//네트워크 상태 설정
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) //15분(주기적) 최소기준이 15분(1000 = 1초)
//                .setMinimumLatency(5000)
                .build();

        //setPeriodic 부팅 후 작업 실행여부설정 RECEIVE_BOOT_COMPLETED 권한설정해야함
        //setMinimumLatency(TimeUnit.MINUTES.toMillis(1))1분 //얼마후에 실행되어야 하는지(한번만)
        //NETWORK_TYPE_UNMETERED WIFI
        //NETWORK_TYPE_CELLULAR 셀룰러
        //NETWORK_TYPE_ANY 언제든
        //NETWORK_TYPE_NONE 인터넷 연결 안되었을때

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        int resultCode = jobScheduler.schedule(info);
        if(resultCode == jobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "작업 성공");
        }else{
            Log.d(TAG, "작업 실패");
        }

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
