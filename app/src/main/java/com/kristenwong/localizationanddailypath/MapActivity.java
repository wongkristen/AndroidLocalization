package com.kristenwong.localizationanddailypath;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CheckInsManager mCheckInsManager;
    private Button mMyLocationButton;
    private double mLat, mLon;
    private MarkerOptions mCurrentLocationMarketOptions;
    private Marker mCurrentLocationMarker;
    private Geocoder mGeocoder;
    private Location mCurrentLocation;
    private LocationManager mLocationManager;
    private AlertDialog.Builder mNearbyAlertBuilder;
    private AlertDialog mNearbyAlert;
    private static final String LAT_KEY = "latitude";
    private static final String LON_KEY = "longitude";
    private static final String MAIN_DEBUG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle bundle = getIntent().getExtras();
        mLat = bundle.getDouble(LAT_KEY);
        mLon = bundle.getDouble(LON_KEY);

        mCurrentLocationMarketOptions = new MarkerOptions().
                position(new LatLng(mLat, mLon)).
                title("Current Location").
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        mMyLocationButton = (Button) findViewById(R.id.button_my_location);
        mCheckInsManager = CheckInsManager.get(getApplicationContext());
        mGeocoder = new Geocoder(this, Locale.getDefault());
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mCurrentLocation.setLatitude(mLat);
        mCurrentLocation.setLongitude(mLon);

        mNearbyAlertBuilder = new AlertDialog.Builder(MapActivity.this);
        mNearbyAlertBuilder.setTitle("You are nearby a saved location");
        mNearbyAlertBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mCurrentLocationMarker.setPosition(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng currentLocation = new LatLng(mLat, mLon);
        mCurrentLocationMarker = mMap.addMarker(mCurrentLocationMarketOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                Log.d(MAIN_DEBUG_TAG, "onMapClick: called");

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);
                alertDialog.setTitle("Add Location Marker");
                alertDialog.setMessage("Enter name for location marker:");

                final EditText editText = new EditText(getApplicationContext());
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(editText.getText().toString())
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        SavedLocation savedLocation = new SavedLocation();
                        savedLocation.setName(editText.getText().toString());
                        savedLocation.setLatitude(latLng.latitude);
                        savedLocation.setLongitude(latLng.longitude);
                        savedLocation.setAddress(getAddress(mGeocoder, latLng.latitude, latLng.longitude));
                        mCheckInsManager.addSavedLocation(savedLocation);

                        marker.setTag(savedLocation);
                    }
                });

                alertDialog.show();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                SavedLocation savedLocation = (SavedLocation) marker.getTag();
                LatLng position = marker.getPosition();
                if (savedLocation != null) {
                    savedLocation.setLatitude(position.latitude);
                    savedLocation.setLongitude(position.longitude);
                }
                mCheckInsManager.updateSavedLocation(savedLocation);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;

                LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentLocationMarker.setPosition(newLocation);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

                List<SavedLocation> savedLocations = mCheckInsManager.getSavedLocations();
                boolean within30m = false;
                for (SavedLocation savedLocation : savedLocations) {
                    Log.d(MAIN_DEBUG_TAG, "Map onLocationChanged: checking within 30m of location - " + savedLocation);
                    if (mCheckInsManager.calculateWithin30m(savedLocation.getLatitude(),
                            savedLocation.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude())) {
                        Log.d(MAIN_DEBUG_TAG, "Map onLocationChanged: found location within 30m, searching for last checkin");

                        CheckIn checkIn = mCheckInsManager.getLatestCheckin(savedLocation.getName());
                        if (checkIn != null) {
                            Log.d(MAIN_DEBUG_TAG, "Map onLocationChanged: obtained lasted checkin, creating alertdialog");
                            mNearbyAlertBuilder.setMessage("Last check in:\n" + checkIn.getName() + "\n" + checkIn.getTime());
                            mNearbyAlert = mNearbyAlertBuilder.create();
                            within30m = true;

                            if (!MapActivity.this.isFinishing()) mNearbyAlert.show();
                        }
                    }
                }
                if (!within30m && mNearbyAlert != null) mNearbyAlert.cancel();


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

        List<SavedLocation> savedLocations = mCheckInsManager.getSavedLocations();
        for (SavedLocation savedLocation: savedLocations) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(savedLocation.getLatitude(), savedLocation.getLongitude()))
                    .radius(500)
                    .fillColor(Color.RED)).setTag(savedLocation);
        }
    }

    private String getAddress(Geocoder geocoder, double latitude, double longitude){
        List<Address> addresses;
        String address = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() == 0) {
                Log.d(MAIN_DEBUG_TAG, "setScreenText: no address available");
                return "No address available";
            }
            else {

                String address1 = (addresses.get(0).getAddressLine(0) == null) ? "" : addresses.get(0).getAddressLine(0);
                String city = (addresses.get(0).getLocality() == null) ? "" : (addresses.get(0).getLocality() + ", ");
                String state = (addresses.get(0).getAdminArea() == null) ? "" : (addresses.get(0).getAdminArea() + " ");
                String zip = (addresses.get(0).getPostalCode() == null) ? "" : addresses.get(0).getPostalCode();
                String address3 = (addresses.get(0).getCountryName() == null) ? "" : addresses.get(0).getCountryName();

                address = address1 + "\n" + city + state + zip + "\n" + address3;
                Log.d(MAIN_DEBUG_TAG, "full address = " + address);
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(MAIN_DEBUG_TAG, "setScreenText: geocoder error");
        }
        return address;
    }
}
