package com.example.localarmv01;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.security.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    SearchView searchView;

    LatLng currentLocation;
    Address address;
    Ringtone ringtone;
    Timer timer = new Timer();

    private static final String CHANNEL_ID = "Location Alarm";

    boolean isFirstTime = true;

    //private NotificationManagerCompat notificationManagerCompat;

    GoogleMap mapAPI;
    SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
         != PackageManager.PERMISSION_GRANTED) {

             ActivityCompat.requestPermissions(MainActivity.this, new String []{Manifest.permission.ACCESS_FINE_LOCATION},
                     REQUEST_CODE_LOCATION_PERMISSION);


         }
         else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                 != PackageManager.PERMISSION_GRANTED){

             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
         }

         else{
             searchView = findViewById(R.id.searchLocation);
             mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);

            /*
             notificationManagerCompat = NotificationManagerCompat.from(this);
             Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("LocAlarm").setContentText("App is running").setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_NAVIGATION).build();
             notificationManagerCompat.notify(1,notification);

             */


             //````````````
             searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                 @Override
                 public boolean onQueryTextSubmit(String query) {
                     String location = searchView.getQuery().toString();
                     List<Address> addressList;
   //                  if(location != null || !location.equals("")){
                         Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                         try {
                             addressList = geocoder.getFromLocationName(location,2);

                             if(addressList.size() > 0) {
                                 address = addressList.get(0);
                                 LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                                 mapAPI.addMarker(new MarkerOptions().position(latLng).title(location));
                                 mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14f));

                                 try{

                                     Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
                                     PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, notificationIntent,0);

                                     if("com.example.localarmv01.UPDATE_LOCATION".equals(notificationIntent.getAction()))
                                     {

                                     }

                                     NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(MainActivity.this.NOTIFICATION_SERVICE);
                                     Notification.Builder builder = new Notification.Builder(MainActivity.this);
                                     builder.setContentTitle("LocAlarm").setContentText("In Background, Application is running.")
                                             .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                                             .setContentIntent(pendingIntent)
                                     .setWhen(System.currentTimeMillis())
                                     .setContentIntent(pendingIntent)
                                     .setAutoCancel(true);

                                     notificationManager.notify(1,builder.build());
                                     createNotificationChannel();


                                 }catch(Exception e){
                                     e.printStackTrace();
                                 }

                                 timer.scheduleAtFixedRate(new TimerTask() {
                                     public void run() {
                                         distanceFromTwoLocation(address);
                                     }
                                 },0,1000);

                             }
                         } catch (IOException e) {
                             e.printStackTrace();
                         }

    //                 }
                     return false;
                 }

                 @Override
                 public boolean onQueryTextChange(String newText) {
                     return false;
                 }
             });
             //````````````

             mapFragment.getMapAsync(this);
         }

    }

    public void onDestroy() {
        ringtone.stop();
        timer.cancel();
        super.onDestroy();
    }

    private void distanceFromTwoLocation(Address address) {

        Location CLocation = new Location("C"); //Current location
        CLocation.setLatitude(currentLocation.latitude);
        CLocation.setLongitude(currentLocation.longitude);

        Location DLocation = new Location("D");  //Destination location
        DLocation.setLatitude(address.getLatitude());
        DLocation.setLongitude(address.getLongitude());

        double distance = CLocation.distanceTo(DLocation);

        distance = distance * 0.001;        //to convert meters into kilometer
        System.out.println("Distance is " + distance + " km");
        ringAlarm(distance);
    }

    private void ringAlarm(double distance) {

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        if(distance < 1){
            ringtone.play();
        }
        else{
            ringtone.stop();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);

            mapFragment.getMapAsync(this);
        }
        else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mapAPI = googleMap;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this).getLastLocation();
                        if((locationResult != null) && (locationResult.getLocations().size() > 0)) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            currentLocation = new LatLng(latitude, longitude);
                            System.out.println("Current Location Lat: " + latitude + " Long: " + longitude);

                            googleMap.setMyLocationEnabled(true);       //add the Blue circle on map with a button of relocating to current position.

                           // mapAPI.addMarker(new MarkerOptions().position(currentLocation));      //add the Red Marker on the map to current position.
                            if(isFirstTime) {
                                mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
                                isFirstTime = false;
                            }

                        }
                    }
                }, Looper.getMainLooper());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("LocAlarm App is running.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
