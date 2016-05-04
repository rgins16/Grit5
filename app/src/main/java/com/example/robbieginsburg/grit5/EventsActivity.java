package com.example.robbieginsburg.grit5;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

public class EventsActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap googleMap;
    private Handler zoomHandler = new Handler();
    private Handler updateLocationHandler = new Handler();

    // defines the North, South, East, and West boundaries of the map
    private final double MAX_NORTH_LATITUDE = 39.262838738025245;
    private final double MAX_SOUTH_LATITUDE = 39.24699456235774;
    private final double MAX_EAST_LONGITUDE = -76.70298434793949;
    private final double MAX_WEST_LONGITUDE = -76.72009818255901;

    private boolean bool = false;
    private double currentLatitude, currentLongitude = 0.0;

    private Button picture, video;

    MyLocationListener location;
    LocationManager lm;

    Uri pictureSubmissionUri, videoSubmissionUri = null;

    private LatLng userLocation = null;

    Calendar calendar;

    EditText pictureText, videoText;

    UpdateUserLocation updateUserLocation;

    Intent homeScreen, events, socialMap, infoMap, phoneBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSocial);
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
        //events = new Intent(this, .class);
        socialMap = new Intent(this, EventsActivity.class);
        //infoMap = new Intent(this, .class);
        //phoneBook = new Intent(this, .class);



        picture = (Button) findViewById(R.id.pictureButton);
        video = (Button) findViewById(R.id.videoButton);

        picture.setOnClickListener(this);
        video.setOnClickListener(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location);

        // creates the map object and enables satellite mode
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // makes the map focus on UMBC
        LatLng UMBC = new LatLng(39.255462, -76.711110);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UMBC, 16));

        //googleMap.addMarker(new MarkerOptions().position(UMBC));
        googleMap.setOnMarkerClickListener(this);

        configureMapSettings();

        //zoomHandler.post(MoniterZoom);

        // start asynctask that constantly updates the user's location
        updateUserLocation = new UpdateUserLocation();
        updateUserLocation.execute();
    }

    // this is the handler that constantly moniters the map to make sure the user doesn't
    // scroll/zoom away from UMBC
    Runnable MoniterZoom = new Runnable() {
        @Override
        public void run() {

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
    public void onClick(View v) {

        // calls the function to create a unique file name
        String fileName = createUniqueFileName();

        switch (v.getId()) {

            case R.id.pictureButton:

                // in the case of a picture, the file is set to the .png type
                if(calendar.get(Calendar.AM_PM) == 0) fileName += "_AM.png";
                else fileName += "_PM.png";

                // specifies the directory for the videos
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Grit_Pictures");
                // if the directory doesn't exist on the device, create it
                if(!pictureDirectory.exists()) pictureDirectory.mkdirs();

                // saves the picture to the user's device
                File pictureFile =
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
                                + "/Grit_Pictures").getAbsolutePath()
                                + fileName);

                // declares a new camera intent to capture a photo
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // gets the Uri of the file
                pictureSubmissionUri = Uri.fromFile(pictureFile);
                //Uri pictureUri = Uri.fromFile(pictureFile);

                // pass the Uri of the video to the intent
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureSubmissionUri);

                // starts the camera intent that captures video
                startActivityForResult(pictureIntent, 1);

                break;

            case R.id.videoButton:

                // in the case of a video, the file is set to the .mp4 type
                if(calendar.get(Calendar.AM_PM) == 0) fileName += "_AM.mp4";
                else fileName += "_PM.mp4";

                // specifies the directory for the videos
                File videoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Grit_Videos");
                // if the directory doesn't exist on the device, create it
                if(!videoDirectory.exists()) videoDirectory.mkdirs();

                // saves the video to the user's device
                File videoFile =
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
                                + "/Grit_Videos").getAbsolutePath()
                                + fileName);

                // declares a new camera intent to capture video
                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                // gets the Uri of the file
                Uri videoUri = Uri.fromFile(videoFile);
                videoSubmissionUri = videoUri;

                // pass the Uri of the video to the intent
                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);

                // 10 MB size limit
                videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10485760L);

                // starts the camera intent that captures video
                startActivityForResult(videoIntent, 2);

                break;
        }
    }

    // method for handling the results of capturing a picture or a video
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if the action was to capture a picture, then store all relevant data in the database
        if (requestCode == 1 && resultCode == RESULT_OK) {

            // creates a new dialog box
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.imagesubmission);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(800, 1220);
            dialog.show();

            // gets rid of the dim that is enabled by default
            dialog.getWindow().setDimAmount(0.0f);

            // sets up the image from xml file
            ImageView image = (ImageView) dialog.findViewById(R.id.imageSubmissionView);
            image.setImageURI(pictureSubmissionUri);

            final String timeStamp = String.valueOf(System.currentTimeMillis());
            pictureText = (EditText) dialog.findViewById(R.id.editTextPicture);
            Button pictureSubmit = (Button) dialog.findViewById(R.id.pictureSubmitButton);

            // click listener for the submit button
            pictureSubmit.setOnClickListener(this);
            pictureSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    // if the user's location via gps can not currently be determined
                    // then do not create the post
                    // cancel, and alert the user
                    if(userLocation != null){

                        // adds a marker for the new post
                        googleMap.addMarker(new MarkerOptions()
                                .position(location.getLatLong())
                                .title(String.valueOf("Picture"))
                                .snippet(timeStamp)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                        // create object and push it to the database
                        // time since epoch
                        // media file
                        // description of media file
                    }
                    //
                    else{
                        Toast.makeText(getApplicationContext(), "No connection to GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // if the method was to capture a video, then store all relevant data in the database
        if (requestCode == 2 && resultCode == RESULT_OK) {

            // creates a new dialog box
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.videosubmission);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(800, 1220);
            dialog.show();

            // gets rid of the dim that is enabled by default
            dialog.getWindow().setDimAmount(0.0f);

            // sets up the videoview from xml file
            VideoView video = (VideoView) dialog.findViewById(R.id.videoSubmissionView);
            video.setVideoURI(data.getData());
            video.start();

            // upon the video being ready to play, the media player will declare to keep
            // looping the video
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });

            final String timeStamp = String.valueOf(System.currentTimeMillis());
            videoText = (EditText) dialog.findViewById(R.id.editTextVideo);
            Button pictureSubmit = (Button) dialog.findViewById(R.id.videoSubmitButton);

            // click listener for the submit button
            pictureSubmit.setOnClickListener(this);
            pictureSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    // if the user's location via gps can not currently be determined
                    // then do not create the post
                    // cancel, and alert the user
                    if(userLocation != null){

                        // adds a marker for the new post
                        googleMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title(String.valueOf("Video"))
                                .snippet(timeStamp)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        // create object and push it to the database
                        // time since epoch
                        // media file
                        // description of media file
                    }
                    //
                    else{
                        Toast.makeText(getApplicationContext(), "No connection to GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // this method uses the calendar to create a unique filename
    public String createUniqueFileName() {
        calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        if(hour == 0) hour = 12;
        return String.valueOf("/" + (calendar.get(Calendar.MONTH) + 1) + "_" +
                calendar.get(Calendar.DATE) + "_" + calendar.get(Calendar.YEAR) + "___" + hour +
                "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // if the user has clicked a marker containing a picture
        if (String.valueOf(marker.getTitle()).equals(String.valueOf("Picture"))){

            // creates a new dialog box
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.viewimagepost);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(800, 1220);
            dialog.show();

            // gets rid of the dim that is enabled by default
            dialog.getWindow().setDimAmount(0.0f);

            // sets up the image from xml file
            ImageView image = (ImageView) dialog.findViewById(R.id.viewImagePost);
            image.setImageURI(pictureSubmissionUri);

            TextView text = (TextView) dialog.findViewById(R.id.viewPictureText);
            text.setText(pictureText.getText().toString());
        }
        // if the user has clicked a marker containing a picture
        else if (String.valueOf(marker.getTitle()).equals(String.valueOf("Video"))){

            // creates a new dialog box
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.viewvideopost);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(800, 1220);
            dialog.show();

            // gets rid of the dim that is enabled by default
            dialog.getWindow().setDimAmount(0.0f);

            // sets up the image from xml file
            VideoView video = (VideoView) dialog.findViewById(R.id.viewVideoPost);
            video.setVideoURI(videoSubmissionUri);
            video.start();

            // upon the video being ready to play, the media player will declare to keep
            // looping the video
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });

            TextView text = (TextView) dialog.findViewById(R.id.viewVideoText);
            text.setText(videoText.getText().toString());
        }

        return true;
    }

    private class UpdateUserLocation extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {return null;}

        @Override
        protected void onPostExecute(String result) {

            // updates the user's current location
            userLocation = location.getLatLong();

            // make the async task repeat itself so it can keep updating the user's location
            updateUserLocation = new UpdateUserLocation();
            updateUserLocation.execute();
        }
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

        } else if (id == R.id.nav_mapSocial) {
            // start the social map activity
            startActivity(socialMap);

        } else if (id == R.id.nav_mapsInfo) {

        } else if (id == R.id.nav_phoneBook) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}