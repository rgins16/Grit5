package com.example.robbieginsburg.grit5;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InfoMaps extends AppCompatActivity implements GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap googleMap;
    private Handler zoomHandler = new Handler();

    //Defines the North, South, East, and West boundaries of the map
    //  Making these less specific so that larger screens don't crap out
    private final double MAX_NORTH_LATITUDE = 39.27;//39.262838738025245;
    private final double MAX_SOUTH_LATITUDE = 39.24;//39.24699456235774;
    private final double MAX_EAST_LONGITUDE = -76.69;//-76.70298434793949;
    private final double MAX_WEST_LONGITUDE = -76.73;//-76.72009818255901;

    private boolean bool = false;
    private double currentLatitude, currentLongitude = 0.0;

    private MyLocationListener location;
    private LocationManager lm;

    private LatLng userLocation = null;

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // disables the title showing the name of the app
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // sets all the intents for if a user clicks one of the buttons in the navbar
        homeScreen = new Intent(this, HomeScreen.class);
        upComing = new Intent(this, UpComing.class);
        happeningNow = new Intent(this, EventsActivity.class);
        infoMap = new Intent(this, InfoMaps.class);
        phoneBook = new Intent(this, PhoneBook.class);


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = new MyLocationListener();

        // ***************** pick the strongest signal gps/wifi

        try
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, location);
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

        // creates the map object and enables satellite mode
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.infoMapFragment)).getMapAsync(this);
    }

    // this is the handler that constantly monitors the map to make sure the user doesn't
    // scroll/zoom away from UMBC
    Runnable MoniterZoom = new Runnable() {
        @Override
        public void run()
        {
            float zoomLevel = googleMap.getCameraPosition().zoom;

            // if the user tries to zoom out beyond max zoom level (16), then reset it back to 16
            // this will prevent the user from zooming out beyond UMBC
            if (zoomLevel < 16.0) {
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(16.0f));
            }

            if (bool) {
                // prevents the user from scrolling too far North
                if (googleMap.getProjection().getVisibleRegion().farLeft.latitude > MAX_NORTH_LATITUDE) {

                    LatLng preventNorth = new LatLng(currentLatitude - 0.0001, currentLongitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(preventNorth));
                }

                // prevents the user from scrolling too far South
                if (googleMap.getProjection().getVisibleRegion().nearRight.latitude < MAX_SOUTH_LATITUDE) {

                    LatLng preventSouth = new LatLng(currentLatitude + 0.0001, currentLongitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(preventSouth));
                }

                // prevents the user from scrolling too far East
                if (googleMap.getProjection().getVisibleRegion().nearRight.longitude > MAX_EAST_LONGITUDE) {

                    LatLng preventEast = new LatLng(currentLatitude, currentLongitude - 0.0001);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(preventEast));
                }

                // prevents the user from scrolling too far West
                if (googleMap.getProjection().getVisibleRegion().farLeft.longitude < MAX_WEST_LONGITUDE) {

                    LatLng preventWest = new LatLng(currentLatitude, currentLongitude + 0.0001);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(preventWest));
                }
            }

            currentLatitude = googleMap.getCameraPosition().target.latitude;
            currentLongitude = googleMap.getCameraPosition().target.longitude;
            bool = true;

            zoomHandler.post(MoniterZoom);
        }
    };

    // method that configures the map to my specified custom settings
    public void configureMapSettings() {
        UiSettings uiSettings = googleMap.getUiSettings();

        // sets up the zoom controls on the bottom right of the screen
        uiSettings.setZoomControlsEnabled(true);

        // disables tilt gestures
        uiSettings.setTiltGesturesEnabled(false);

        // disables rotate gestures
        uiSettings.setRotateGesturesEnabled(false);

        // disables the gps and navigation buttons on the bottom right of the screen
        uiSettings.setMapToolbarEnabled(false);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("WWWWWWWWWWWWWW", "" + marker.getTitle());
        if (marker.getTitle().substring(0,3).equals("aaa")) Log.d("GGGGGGGGGGGGGGG", "");

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap_) {
        googleMap = googleMap_;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // makes the map focus on UMBC
        LatLng UMBC = new LatLng(39.255462, -76.711110);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UMBC, 16));

        //googleMap.addMarker(new MarkerOptions().position(new LatLng(39.253466, -76.712760))); // Sondheim
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(39.253855, -76.714265))); // ITE
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(39.254569, -76.713965))); // Egineering

        googleMap.addMarker(new MarkerOptions().title("aaa").position(new LatLng(39.253466, -76.712760)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        googleMap.setOnMarkerClickListener(this);

        configureMapSettings();

        zoomHandler.post(MoniterZoom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        zoomHandler.removeCallbacks(MoniterZoom);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(location);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            //start the home screen activity
            startActivity(homeScreen);

        } else if (id == R.id.nav_events) {
            //start the upcoming events activity
            startActivity(upComing);

        } else if (id == R.id.nav_mapSocial) {
            // start the happening now activity
            startActivity(happeningNow);

        } else if (id == R.id.nav_mapsInfo) {
            //start the info map activity
            startActivity(infoMap);

        } else if (id == R.id.nav_phoneBook) {
            //start the phone book activity
            startActivity(phoneBook);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}