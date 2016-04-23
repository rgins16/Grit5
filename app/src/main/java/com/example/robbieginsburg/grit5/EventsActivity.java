package com.example.robbieginsburg.grit5;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventsActivity extends AppCompatActivity implements View.OnClickListener, GoogleMap.OnMarkerClickListener {

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

    private PopupWindow popUpWindow;
    private LayoutInflater layoutInflator;
    private RelativeLayout relativeLayout;

    MyLocationListener location;
    LocationManager lm;

    Uri pictureUri;

    private LatLng currentLocation, lastUpdatedLocation = null;

    private Marker currentLocationMarker = null;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        relativeLayout = (RelativeLayout) findViewById(R.id.relative);

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

        googleMap.addMarker(new MarkerOptions().position(UMBC));
        googleMap.setOnMarkerClickListener(this);

        configureMapSettings();

        //zoomHandler.post(MoniterZoom);
        updateLocationHandler.post(UpdateCurrentLocation);
    }

    // this is the handler that gets the current location
    Runnable UpdateCurrentLocation = new Runnable() {
        @Override
        public void run() {

            // this handler will use an asynctask to get the current location
            UpdateLocation updateLocation = new UpdateLocation();
            updateLocation.execute();

            // the handler will run again every 3 seconds
            // therefore, the user's current location will be updated every 3 seconds
            updateLocationHandler.postDelayed(UpdateCurrentLocation, 3000);
        }
    };

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
    }

    // method for handling the results of capturing a picture or a video
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if the action was to capture a picture, then store all relevant data in the database
        if (requestCode == 1 && resultCode == RESULT_OK) {


            Toast.makeText(this, "Picture Received", Toast.LENGTH_SHORT).show();
        }

        // if the method was to capture a video, then store all relevant data in the database
        if (requestCode == 2 && resultCode == RESULT_OK) {


            Toast.makeText(this, "Video Received", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // if the user clicked the picture button, open the camera and capture a picture
            case R.id.pictureButton:

                // camera intent
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // specifies the directory for the pictures
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Grit Pictures");

                // if the directory doesn't exist on the device, add it
                if(!pictureDirectory.exists()) pictureDirectory.mkdirs();

                // this chunk of code names the file based on the current date and time
                calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR);
                if(hour == 0) {
                    hour = 12;
                }
                String pictureName = String.valueOf((calendar.get(Calendar.MONTH) + 1) + ":" +
                        calendar.get(Calendar.DATE) + ":" + calendar.get(Calendar.YEAR) + "_" +
                        hour + ":" + calendar.get(Calendar.MINUTE));
                if(calendar.get(Calendar.AM_PM) == 0){pictureName += "_AM.png";}
                else{pictureName += "_PM.png";}


                // the name of the picture will be the time since epoch in milliseconds
                //String pictureName = String.valueOf(System.currentTimeMillis() + ".png");
                File capturedPicture = new File(pictureDirectory, pictureName);
                pictureUri = Uri.fromFile(capturedPicture);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
                startActivityForResult(cameraIntent, 1);
                break;
            // if the user clicked video, open the recorder and capture a video
            case R.id.videoButton:
                Intent video = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(video, 2);
                break;
        }
    }

    // updates the user's location
    public void updateUserLocation() {

        LatLng newestLocation = currentLocation;

        // the very first time the method is called, add the user's current location
        // to the map as a blue marker, and zoom to it
        if (currentLocationMarker == null) {

            // update the marker for the user's new location
            currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(newestLocation)
                    .icon(BitmapDescriptorFactory.
                            defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            // if the user's location has changed
            if (!newestLocation.equals(lastUpdatedLocation)) {

                // remove the current user's location marker
                currentLocationMarker.remove();

                // update the marker for the user's new location
                currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(newestLocation)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // update the user's location
                lastUpdatedLocation = newestLocation;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        layoutInflator = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflator.inflate(R.layout.imagecontainer, null);

        ImageView image = (ImageView) container.findViewById(R.id.imageView);
        image.setImageURI(pictureUri);

        popUpWindow = new PopupWindow(container, 800, 1220, true);
        popUpWindow.setBackgroundDrawable(new BitmapDrawable());
        popUpWindow.setOutsideTouchable(true);
        popUpWindow.showAtLocation(relativeLayout, Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);

        return false;
    }

    private class UpdateLocation extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            while (location.getLatLong() == null) {
                // do nothing and wait until there is a location
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            // updates the user's current location
            currentLocation = location.getLatLong();

            // calls the function to change the marker for the user's location
            updateUserLocation();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoomHandler.removeCallbacks(MoniterZoom);
        updateLocationHandler.removeCallbacks(UpdateCurrentLocation);

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
}

