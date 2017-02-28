package com.example.android.sunshine.wear;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.sunshine.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Salil on 30-01-2017.
 * Service to periodically send weather data to Android Wear
 */

public class SendDataService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleClient;

    private static Intent mIntent;

    private Handler mHandler = null;
    private static Runnable mRunnable = null;
    private static int mWeatherCode = 0, mMax = 0, mMin = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntent = intent;

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleClient.connect();

        mHandler = new Handler();
        mRunnable = new Runnable() {
            public void run() {

                if (mGoogleClient.isConnected()) {
                    // Retrieve weather records
                    String URL = "content://com.example.android.sunshine/weather";

                    Uri weatherUri = Uri.parse(URL);
                    Cursor c = getContentResolver().query(weatherUri, null, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        mWeatherCode = c.getInt(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
                        mMax = (int) c.getFloat(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                        mMin = (int) c.getFloat(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
                        c.close();
                    }

                    String message = String.format("%d,%d,%d", mWeatherCode, mMax, mMin);
                    new SendToDataLayerThread("/message_path", message, mGoogleClient).start();
                }
                else {
                    mGoogleClient.connect();
                }

                //Update timer
                mHandler.postDelayed(mRunnable, 3 * 60 * 60 * 1000);
            }
        };

        mHandler.postDelayed(mRunnable, 1500);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mGoogleClient && mGoogleClient.isConnected()) {
            mGoogleClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        String message = String.format("%d,%d,%d", mWeatherCode, mMax, mMin);
        new SendToDataLayerThread("/message_path", message, mGoogleClient).start();
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}
