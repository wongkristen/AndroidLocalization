package com.kristenwong.localizationanddailypath;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {
    private LocationManager mLocationManager;
    private double mLat, mLon;
    private Geocoder mGeocoder;
    private CheckInsManager mCheckInsManager;
    private static final int FIVE_MIN_INTERVAL = 1000 * 60 * 5;
    private static final int HALF_MIN_INTERVAL = 1000 * 10;
    private static final String SERVICE_LAT_KEY = "service lat";
    private static final String SERVICE_LON_KEY = "service lon";
    private static final String ACTION_UPDATE_LIST = "action update list";
    private static final String MAIN_DEBUG_TAG = "MainActivity";

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Double lat = intent.getDoubleExtra(SERVICE_LAT_KEY, 0);
        Double lon = intent.getDoubleExtra(SERVICE_LON_KEY, 0);
        mLat = lat;
        mLon = lon;

        Log.d(MAIN_DEBUG_TAG, "LocationService: onStartCommand, mLat = " + mLat + ", mLon = " + mLon);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

            }
        });
        thread.start();

        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(MAIN_DEBUG_TAG, "LocationService: 5 min check in (outside first thread)");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                CheckIn checkIn = createCheckIn(location);
                mCheckInsManager.addCheckIn(checkIn);

                Intent notifyListUpdate = new Intent();
                notifyListUpdate.setAction(ACTION_UPDATE_LIST);
                sendBroadcast(notifyListUpdate);

                handler.postDelayed(this, FIVE_MIN_INTERVAL);
            }
        }, FIVE_MIN_INTERVAL);

        final Handler locationChangeHandler = new Handler();
        locationChangeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(MAIN_DEBUG_TAG, "LocationService: location change check (outside first thread)");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(MAIN_DEBUG_TAG, "LocationService: location change check, current location = " + location);

                if (location != null && !checkWithin100m(mLat, mLon, location.getLatitude(), location.getLongitude())) {
                    Log.d(MAIN_DEBUG_TAG, "LocationService: location outside 100m range, creating check in");
                    CheckIn checkIn = createCheckIn(location);
                    if (checkIn != null) mCheckInsManager.addCheckIn(checkIn);
                    mLat = location.getLatitude();
                    mLon = location.getLongitude();

                    Intent notifyListUpdate = new Intent();
                    notifyListUpdate.setAction(ACTION_UPDATE_LIST);
                    sendBroadcast(notifyListUpdate);
                }

                locationChangeHandler.postDelayed(this, HALF_MIN_INTERVAL);
            }
        }, HALF_MIN_INTERVAL);

        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mCheckInsManager = CheckInsManager.get(getApplicationContext());
        mGeocoder = new Geocoder(this, Locale.getDefault());

    }

    private boolean checkWithin100m(double lat1, double lon1, double lat2, double lon2) {
        float ans[] = {1, 1, 1};
        Location.distanceBetween(lat1, lon1, lat2, lon2, ans);
        return (ans[0] <= 100);
    }

    private CheckIn createCheckIn(Location location) {
        List<Address> addresses;
        try {
            addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() != 0) {
                String address1 = (addresses.get(0).getAddressLine(0) == null) ? "" : addresses.get(0).getAddressLine(0);
                String city = (addresses.get(0).getLocality() == null) ? "" : (addresses.get(0).getLocality() + ", ");
                String state = (addresses.get(0).getAdminArea() == null) ? "" : (addresses.get(0).getAdminArea() + " ");
                String zip = (addresses.get(0).getPostalCode() == null) ? "" : addresses.get(0).getPostalCode();
                String address3 = (addresses.get(0).getCountryName() == null) ? "" : addresses.get(0).getCountryName();

                String fullAddress = address1 + "\n" + city + state + zip + "\n" + address3;

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                String time = simpleDateFormat.format(now);

                return new CheckIn("Auto Check In", location.getLatitude(), location.getLongitude(), fullAddress, time);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
