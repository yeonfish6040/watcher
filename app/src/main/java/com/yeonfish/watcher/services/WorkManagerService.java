package com.yeonfish.watcher.services;

import android.Manifest;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yeonfish.watcher.util.sql.SQLQuery;
import com.yeonfish.watcher.util.sql.SQLResults;

import java.util.function.Consumer;

public class WorkManagerService extends Worker {
    final String TAG = "WorkManagerService";

    public WorkManagerService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        doBackgroundWork();

        return Result.success();
    }

    private void doBackgroundWork() {
        LocationManager manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
//        String bestProvider = String.valueOf(manager.getBestProvider(criteria, true)).toString();
        String bestProvider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.getCurrentLocation(bestProvider, null, getApplicationContext().getMainExecutor(), new Consumer<Location>() {
            @Override
            public void accept(Location location) {
                updatePos(location);
            }
        });
        Log.d(TAG, "Job finished");
    }

    protected void updatePos(Location location) {
        Log.e("TAG", "GPS is on");
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        new Thread(() -> {
            SQLQuery sqlQuery = new SQLQuery("lyj.kr", "3306", "android", "f60ed56a9c8275894022fe5a7a1625c33bdb55b729bb4e38962af4d1613eda25", "android");
            if (!sqlQuery.cStatus()) {
                toastOnThread(getApplicationContext(), "Fail1", Toast.LENGTH_LONG);
                return;
            }
            SQLResults results = null;
            try {
                String[][] param = {{"time", "lat", "lng"}, {String.valueOf(System.currentTimeMillis()), latitude.toString(), longitude.toString()}};
                sqlQuery.delete("MyPos", "`time` < "+String.valueOf(System.currentTimeMillis()-7200000));
                sqlQuery.insert("MyPos", param);
            } catch (Exception e) {
                toastOnThread(getApplicationContext(), "Fail2", Toast.LENGTH_LONG);
                throw new RuntimeException(e);
            }
        }).start();
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
