package com.yeonfish.watcher.services;

import android.Manifest;
import android.app.Activity;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.yeonfish.watcher.MainActivity;
import com.yeonfish.watcher.util.sql.SQLQuery;
import com.yeonfish.watcher.util.sql.SQLResults;

public class JobService extends android.app.job.JobService {
    final String TAG = "JobService";

    FusedLocationProviderClient locationProviderClient;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: " + params.getJobId());

        // initalize location provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0 || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
            locationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    uploadPos(location);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: "+params.getJobId());
        return true;
    }

    protected void uploadPos(Location location) {
        Log.e("TAG", "GPS is on");
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        new Thread(() -> {
            SQLQuery sqlQuery = new SQLQuery("lyj.kr", "3306", "android", "f60ed56a9c8275894022fe5a7a1625c33bdb55b729bb4e38962af4d1613eda25", "android");
            if (!sqlQuery.cStatus()) {
                toastOnThread(JobService.this, "Fail1", Toast.LENGTH_LONG);
                return;
            }
            SQLResults results = null;
            try {
                String[][] param = {{"time", "lat", "lng"}, {String.valueOf(System.currentTimeMillis()), latitude.toString(), longitude.toString()}};
                sqlQuery.delete("MyPos", "`time` < "+String.valueOf(System.currentTimeMillis()-7200000));
                sqlQuery.insert("MyPos", param);
            } catch (Exception e) {
                toastOnThread(JobService.this, "Fail2", Toast.LENGTH_LONG);
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
