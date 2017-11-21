package com.kristenwong.localizationanddailypath;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private double lat, lon;
    private TextView mLatitude, mLongitude, mAddress1, mAddress2, mAddress3;
    private ListView mCheckInList;
    private Button mCheckInButton, mViewMapButton;
    private Context mContext;
    private CheckInsManager mCheckInsManager;
    private CheckInListAdapter mAdapter;
    private static final String MAIN_DEBUG_TAG = "MainActivity";
    private static final String LAT_KEY = "latitude";
    private static final String LON_KEY = "longitude";
    private static final String SERVICE_LAT_KEY = "service lat";
    private static final String SERVICE_LON_KEY = "service lon";
    private static final String ACTION_UPDATE_LIST = "action update list";
    private String mFullAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mCheckInsManager = CheckInsManager.get(mContext);

        mLatitude = (TextView) findViewById(R.id.text_latitude_value);
        mLongitude = (TextView) findViewById(R.id.text_longitude_value);
        mAddress1 = (TextView) findViewById(R.id.text_address_1);
        mCheckInButton = (Button) findViewById(R.id.button_checkin);
        mViewMapButton = (Button) findViewById(R.id.button_view_map);
        mCheckInList = (ListView) findViewById(R.id.listview_checkins);

        mAdapter = new CheckInListAdapter(mCheckInsManager.getCheckIns(), getApplicationContext());
        mCheckInList.setAdapter(mAdapter);

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setLatAndLong(locationManager);
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        setScreenText(geocoder);

        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra(SERVICE_LAT_KEY, lat);
        serviceIntent.putExtra(SERVICE_LON_KEY, lon);
        startService(serviceIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_LIST);
        registerReceiver(new LocationServiceBroadcastReceiver(), filter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*3, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setLatAndLong(locationManager);
                setScreenText(geocoder);

                Log.d(MAIN_DEBUG_TAG, "onLocationChanged called");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Add Check In");
                alertDialog.setMessage("Enter name for check in:");

                final EditText editText = new EditText(getApplicationContext());
                editText.setPadding(20, 0, 20, 0);
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String checkInName = editText.getText().toString();
                        android.text.format.DateFormat df = new android.text.format.DateFormat();
                        df.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date());

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                        Calendar calendar = Calendar.getInstance();
                        Date now = calendar.getTime();
                        String time = simpleDateFormat.format(now);

                        CheckIn checkIn = new CheckIn(checkInName, lat, lon, mFullAddress, time);
                        mCheckInsManager.addCheckIn(checkIn);
                        mAdapter.updateList(mCheckInsManager.getCheckIns());

                        List<CheckIn> checkInList = mCheckInsManager.getCheckIns();
                        for (CheckIn c: checkInList) {
                            Log.d(MAIN_DEBUG_TAG, "saved check in: " + c.getName() + " " + c.getTime() + " " + c.getLatitude() + " " + c.getLongitude() + " " + c.getUuid());
                        }
                    }
                });

                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();

            }


        });

        mViewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLatAndLong(locationManager);

                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra(LAT_KEY, lat);
                intent.putExtra(LON_KEY, lon);
                startActivity(intent);
            }
        });
    }

    private Location getLocation(LocationManager locationManager){

        boolean gpsEnabled = false, networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gpsEnabled && !networkEnabled) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
            alert.setMessage("GPS Not Enabled!");
            alert.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void setLatAndLong(LocationManager locationManager){
        Location location = getLocation(locationManager);
        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null){
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
        Log.d(MAIN_DEBUG_TAG, "setLatAndLong: location = " + location);
    }

    private void setScreenText(Geocoder geocoder){
        Log.d(MAIN_DEBUG_TAG, "setScreenText: " + lat + " " + lon);
        mLatitude.setText(Double.toString(lat));
        mLongitude.setText(Double.toString(lon));

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses.size() == 0) {
                String message = "No address available";
                mAddress1.setText(message);
                Log.d(MAIN_DEBUG_TAG, "setScreenText: no address available");
            }
            else {

                String address1 = (addresses.get(0).getAddressLine(0) == null) ? "" : addresses.get(0).getAddressLine(0);
                String city = (addresses.get(0).getLocality() == null) ? "" : (addresses.get(0).getLocality() + ", ");
                String state = (addresses.get(0).getAdminArea() == null) ? "" : (addresses.get(0).getAdminArea() + " ");
                String zip = (addresses.get(0).getPostalCode() == null) ? "" : addresses.get(0).getPostalCode();
                String address3 = (addresses.get(0).getCountryName() == null) ? "" : addresses.get(0).getCountryName();

                mFullAddress = address1 + "\n" + city + state + zip + "\n" + address3;
                mAddress1.setText(mFullAddress);
                Log.d(MAIN_DEBUG_TAG, "full address = " + mFullAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(MAIN_DEBUG_TAG, "setScreenText: geocoder error");
        }

    }

    private class LocationServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(MAIN_DEBUG_TAG, "MainActivity: receiving service broadcast");
            if (intent.getAction().equals(ACTION_UPDATE_LIST)) {
                Log.d(MAIN_DEBUG_TAG, "MainActivity: received service broadcast, updating list");
                List<CheckIn> checkIns = mCheckInsManager.getCheckIns();
                mAdapter.updateList(checkIns);
            }
        }
    }
}
