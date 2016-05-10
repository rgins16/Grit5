package com.example.robbieginsburg.grit5;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.PointF;
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
import android.util.FloatMath;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
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
        GoogleMap.OnInfoWindowClickListener, NavigationView.OnNavigationItemSelectedListener {

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

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook;

    Marker accService, admin, aokLib, bio, dHall, engineering, fineArts, fM, greenHouse, ite,
            mathPsych, meyerhoff, pahb, physics, publicPolicy, rac, sherman, sondheim, commons,
            trc, uniCenter;

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

        // enables me to put my custom images in the navbar
        navigationView.setItemIconTintList(null);

        // sets all the intents for if a user clicks one of the buttons in the navbar
        homeScreen = new Intent(this, HomeScreen.class);
        upComing = new Intent(this, UpComing.class);
        happeningNow = new Intent(this, EventsActivity.class);
        infoMap = new Intent(this, InfoMaps.class);
        phoneBook = new Intent(this, PhoneBook.class);


        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.infoMapFragment))
                .getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        configureMapSettings();

        createMarkers();

        // makes the map focus on UMBC
        LatLng UMBC = new LatLng(39.255462, -76.711110);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UMBC, 16));

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        location = new MyLocationListener();

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
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, location);

        zoomHandler.post(MoniterZoom);
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

    // creates all the markers
    public void createMarkers(){

        accService = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.253954, -76.711055))
                .title("Academic Services")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        admin = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.253039, -76.713515))
                .title("Administration")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        aokLib = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.256625, -76.711336))
                .title("A.O.K. Library")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        bio = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254847, -76.712108))
                .title("Biological Science")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        commons = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.255071, -76.710981))
                .title("The Commons")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        dHall = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.255918, -76.707677))
                .title("True Grits")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        engineering = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254539, -76.713996))
                .title("Engineering")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        fineArts = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.255133, -76.713458))
                .title("Fine Arts")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        fM = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.252706, -76.704422))
                .title("Facilities Management")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        greenHouse = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.258181, -76.713568))
                .title("Green House")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        ite = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.253837, -76.714228))
                .title("Information Technology/Engineering")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        mathPsych = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254104, -76.712482))
                .title("Math/Psychology")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        meyerhoff = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254941, -76.712782))
                .title("Meyerhoff Chemistry")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        pahb = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.255266, -76.715287))
                .title("Performing Arts/Humanities")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        physics = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254481, -76.709669))
                .title("Physics")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        publicPolicy = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.255150, -76.709132))
                .title("Public Policy")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        rac = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.252866, -76.712497))
                .title("Retriever Activity Center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        sherman = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.253593, -76.713191))
                .title("Sherman Hall")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        sondheim = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.253466, -76.712760))
                .title("Sondheim Hall")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        trc = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254692, -76.702479))
                .title("Technology Research Center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        uniCenter = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.254334, -76.713282))
                .title("University Center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        final Dialog dialog = new Dialog(InfoMaps.this) {
            @Override
            public void dismiss() {
                super.dismiss();
            }
        };

        if(marker.getTitle().equals(accService.getTitle())){
            //dialog.setContentView(R.layout.view_acd_service);
        }
        else if(marker.getTitle().equals(admin.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(aokLib.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(commons.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(dHall.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(engineering.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(fineArts.getTitle())){
            dialog.setContentView(R.layout.view_fine_arts);
        }
        else if(marker.getTitle().equals(fM.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(greenHouse.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(ite.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(mathPsych.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(meyerhoff.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(pahb.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(physics.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(publicPolicy.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(rac.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(sherman.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(sondheim.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(trc.getTitle())){
            //dialog.setContentView(R.layout.viewvideopost);
        }
        else if(marker.getTitle().equals(uniCenter.getTitle())){
            dialog.setContentView(R.layout.view_uni_center);
        }

        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(1000, 1520);
        dialog.show();

        // gets rid of the dim that is enabled by default
        dialog.getWindow().setDimAmount(0.0f);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        return false;
    }
}