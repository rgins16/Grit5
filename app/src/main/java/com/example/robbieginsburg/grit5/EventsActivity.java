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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
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

public class EventsActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, LocationListener{

    private GoogleMap googleMap;
    private Handler zoomHandler = new Handler();

    //Defines the North, South, East, and West boundaries of the map
    //  Making these less specific so that larger screens don't crap out
    private final double MAX_NORTH_LATITUDE = 39.27;//39.262838738025245;
    private final double MAX_SOUTH_LATITUDE = 39.24;//39.24699456235774;
    private final double MAX_EAST_LONGITUDE = -76.69;//-76.70298434793949;
    private final double MAX_WEST_LONGITUDE = -76.73;//-76.72009818255901;

    private static final long EXPIRES_AFTER = 5*60; //The number of seconds for data to expire
    private static final int GET_CONTENT = 1;

    private boolean bool = false;
    private double currentLatitude, currentLongitude = 0.0;

    private Button picture, video, addMedia;

    private LocationManager lm;

    private Uri content_Uri;
    private Button submitButton;

    private String UMBC_username;
    private String current_content = null;

    private LatLng userLocation = null;

    private EditText descriptionText;

    //These prevent us from having to loop through the database
    //  This maps uniqueID to marker
    private HashMap<String, Marker> markers;
    //  This maps title to uniqueID (Firebase)
    private HashMap<String, String> uniqueIDs;
    //  This maps uniqueID to descriptions
    private HashMap<String, String> descriptions;

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook;

    private Firebase ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
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


        //picture = (Button) findViewById(R.id.pictureButton);
        //video = (Button) findViewById(R.id.videoButton);
        addMedia = (Button) findViewById(R.id.mediaButton);

        //picture.setOnClickListener(this);
        //video.setOnClickListener(this);
        addMedia.setOnClickListener(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // ***************** pick the stringest signal gps/wifi

        try
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

        // creates the map object and enables satellite mode
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        //Set up the Firebase, and place the markers
        markers = new HashMap<>();
        uniqueIDs = new HashMap<>();
        descriptions = new HashMap<>();

        Intent this_intent = getIntent();
        if(this_intent != null) UMBC_username = this_intent.getStringExtra("UMBC_email");

        // set this equal to the google username
        UMBC_username = "lmc3";
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

    //Method for shooting off the AddMedia intent
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.mediaButton:
                // starts the activity that add the media to the database
                Intent addMedia = new Intent(this, AddMedia.class);
                addMedia.putExtra("UMBC_Email", UMBC_username);
                startActivityForResult(addMedia, GET_CONTENT);
                break;
        }
    }

    //Method for handling the results of capturing a picture or a video
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if the action was to capture a picture, then store all relevant data in the database
        if (requestCode == GET_CONTENT && resultCode == RESULT_OK)
        {
            //This has a default, but it *should* never be used
            boolean isVideo = data.getBooleanExtra("IS_VIDEO", false);
            //This is the returned seconds since epoch (from when the content was created)
            final long secsSinceEpoch = data.getLongExtra("SECS_EPOCH", 0);
            final String type = isVideo ? "Video" : "Picture";

            // creates a new dialog box
            final Dialog dialog = new Dialog(this);

            //This has been made reliable (and uniform)
            content_Uri = data.getData();

            if(!isVideo) //If the content is a picture
            {
                dialog.setContentView(R.layout.imagesubmission);
                // sets up the image from xml file
                ImageView image = (ImageView) dialog.findViewById(R.id.imageSubmissionView);
                image.setImageURI(content_Uri);

                descriptionText = (EditText) dialog.findViewById(R.id.editTextPicture);
                descriptionText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        descriptionText.setText("");
                        showSoftKeyboard(descriptionText);
                        return false;
                    }
                });
                submitButton = (Button) dialog.findViewById(R.id.pictureSubmitButton);
            }
            else    //This is a video
            {
                dialog.setContentView(R.layout.videosubmission);
                // sets up the videoview from xml file
                VideoView video = (VideoView) dialog.findViewById(R.id.videoSubmissionView);
                video.setVideoURI(content_Uri);
                video.start();

                // upon the video being ready to play, the media player will declare to keep
                // looping the video
                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });

                descriptionText = (EditText) dialog.findViewById(R.id.editTextVideo);
                descriptionText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        descriptionText.setText("");
                        showSoftKeyboard(descriptionText);
                        return false;
                    }
                });

                submitButton = (Button) dialog.findViewById(R.id.videoSubmitButton);
            }
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setLayout(800, 1220);
            dialog.show();
            dialog.getWindow().setDimAmount(0.0f);  // gets rid of the dim that is enabled by default
            // click listener for the submit button
            submitButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();

                    // if the user's location via gps can not currently be determined
                    // then do not create the post
                    // cancel, and alert the user
                    LatLng cur_loc = userLocation;
                    if (cur_loc != null)
                    {
                        String media;
                        try {
                            InputStream is = getContentResolver().openInputStream(content_Uri);
                            byte[] bytes;
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            try {
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bytes = output.toByteArray();
                            media = Base64.encodeToString(bytes, Base64.DEFAULT);
                            // description of media file
                            String desc = descriptionText.getText().toString();
                            //Log.d("Location", cur_loc.toString());
                            Content picture = new Content(type, cur_loc.longitude, cur_loc.latitude,
                                    secsSinceEpoch, media, desc, UMBC_username);
                            ref.push().setValue(picture);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No connection to GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //We need to get the filename for this marker
        String title = marker.getTitle();
        final String uniqueID = uniqueIDs.get(title);

        // if the user has clicked a marker containing a picture
        if (title.substring(0, 7).equals("Picture")){

            String filename = title + ".png";

            current_content = filename;
            final File pictureFile = getContentFile("Pictures", filename);
            Log.d("MarkerClicked", filename);

            //If the file doesn't exist, we have to pull from the database, and write to the file
            Log.d("MarkerClicked", uniqueID);

            if(!pictureFile.exists())
            {
                final Firebase refChild = ref.child(uniqueID);
                refChild.addListenerForSingleValueEvent(new ValueEventListener() {

                    //We need to grab the data and write it, and save the description
                    //  (which is done in the ASyncTask)
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Ref.OnChildAdded", "Made it");
                        String data = dataSnapshot.child("data").getValue(String.class);
                        WriteDataToFile writer = new WriteDataToFile(data, null, false);
                        writer.execute(pictureFile);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        //This sucks
                    }
                });
            }
            else
            {
                //This is a hack to make the code flexible
                WriteDataToFile writer = new WriteDataToFile(null, uniqueID, false);
                writer.execute(pictureFile);
            }
        }
        // if the user has clicked a marker containing a video
        else if (title.substring(0, 5).equals("Video"))
        {
            String filename = title + ".mp4";

            current_content = filename;
            final File videoFile = getContentFile("Videos", filename);
            Log.d("MarkerClicked", filename);

            //If the file doesn't exist, we have to pull from the database, and write to the file
            Log.d("MarkerClicked", uniqueID);

            //I put all of the work in an ASyncTask, in case we have to write to a file
            //If the file doesn't exist, we need to deal with it accordingly
            if(!videoFile.exists())
            {
                final Firebase refChild = ref.child(uniqueID);
                refChild.addListenerForSingleValueEvent(new ValueEventListener() {

                    //We need to grab the data and write it, and save the description
                    //  (which is done in the ASyncTask)
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Ref.OnChildAdded", "Made it");
                        String data = dataSnapshot.child("data").getValue(String.class);
                        WriteDataToFile writer = new WriteDataToFile(data, uniqueID, true);
                        writer.execute(videoFile);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        //This sucks
                    }
                });
            }
            else
            {
                //This is a hack to make the code flexible
                WriteDataToFile writer = new WriteDataToFile(null, uniqueID, true);
                writer.execute(videoFile);
            }
        }
        return true;
    }

    //Type should be "Videos" or "Pictures"
    private File getContentFile(String type, String filename) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Grit_" + type);
        // if the directory doesn't exist on the device, create it
        if(!directory.exists())
        {
            directory.mkdirs();
        }
        // returns the place to save the file on the user's device
        return new File(directory.getAbsolutePath() + "/" + filename);
    }

    //These 4 methods are for LocationListener
    //  For a robust app, these potentially would have more in them
    //  But we're in a time crunch
    @Override
    public void onLocationChanged(Location location) {
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        //Log.d("OnLocationChanged", userLocation.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class WriteDataToFile extends AsyncTask<File, Void, Uri> {

        private String data, uniqueID;
        private boolean is_Video;

        public WriteDataToFile(String data_, String unique_ID, boolean video) {
            data = data_;
            uniqueID = unique_ID;
            is_Video = video;
        }

        @Override
        protected Uri doInBackground(File... params) {

            Uri ret = null;
            File outputFile = params[0];
            if(data != null)
            {
                byte[] contentAsBytes = Base64.decode(data, 0);
                boolean wrote = false;
                FileOutputStream os = null;
                try
                {
                    os = new FileOutputStream(outputFile);
                    os.write(contentAsBytes);
                    os.flush();
                    os.close();
                    os = null;
                    //The write worked
                    ret = Uri.fromFile(outputFile);
                }
                catch (IOException e)
                {
                    //This sucks
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (os != null)
                        {
                            os.close();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else //We didn't need to write
            {
                //All we have to do here is return the Uri and make desc the description
                ret = Uri.fromFile(outputFile);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if(uri != null)
            {
                if(is_Video) { //Video
                    final Dialog dialog = new Dialog(EventsActivity.this) {
                        @Override
                        public void dismiss() {
                            super.dismiss();
                            current_content = null;
                        }
                    };
                    dialog.setContentView(R.layout.viewvideopost);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.getWindow().setLayout(800, 1220);
                    dialog.show();

                    // gets rid of the dim that is enabled by default
                    dialog.getWindow().setDimAmount(0.0f);

                    // sets up the image from xml file
                    VideoView video = (VideoView) dialog.findViewById(R.id.viewVideoPost);
                    video.setVideoURI(uri);
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
                    text.setText(descriptions.get(uniqueID));
                }
                else //Picture
                {
                    final Dialog dialog = new Dialog(EventsActivity.this) {
                        @Override
                        public void dismiss() {
                            super.dismiss();
                            current_content = null;
                        }
                    };
                    dialog.setContentView(R.layout.viewimagepost);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.getWindow().setLayout(800, 1220);
                    dialog.show();

                    // gets rid of the dim that is enabled by default
                    dialog.getWindow().setDimAmount(0.0f);

                    // sets up the image from xml file
                    ImageView image = (ImageView) dialog.findViewById(R.id.viewImagePost);
                    image.setImageURI(uri);

                    TextView text = (TextView) dialog.findViewById(R.id.viewPictureText);
                    text.setText(descriptions.get(uniqueID));

                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap_) {
        googleMap = googleMap_;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // makes the map focus on UMBC
        LatLng UMBC = new LatLng(39.255462, -76.711110);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UMBC, 16));

        //googleMap.addMarker(new MarkerOptions().position(UMBC));
        googleMap.setOnMarkerClickListener(this);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://gritapptest4112016.firebaseio.com").child("content");

        Log.d("Hello", "Hello");

        ref.addChildEventListener(
                new ChildEventListener() {
                    //This is triggered for every existing element and every one
                    //  that is added
                    // every time the app is loaded every object in the db will be written to a marker
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String uniqueID = dataSnapshot.getKey();
                        //Log.d("OnChildAdded", uniqueID);
                        //This is causing us problems. We're downloading the WHOLE file...

                        /*double latitude, longitude;
                        String desc, user, type;
                        long time;
                        latitude = dataSnapshot.child("latitude").getValue(Double.class);
                        longitude = dataSnapshot.child("longitude").getValue(Double.class);
                        time = dataSnapshot.child("time").getValue(Long.class);
                        desc = dataSnapshot.child("desc").getValue(String.class);
                        user = dataSnapshot.child("user").getValue(String.class);
                        type = dataSnapshot.child("type").getValue(String.class);*/
                        Content c = dataSnapshot.getValue(Content.class);
                        //Content c = new Content(type, longitude, latitude, time, null, desc, user);
                        placeMarker(c, uniqueID);
                        Log.d("Content: ", c.toString());
                    }

                    //This tells us that we need to remove a Marker and delete the
                    //  temp file (when it is safe to do so)
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                        String uniqueID = dataSnapshot.getKey();
                        removeMarker(uniqueID);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                }
        );

        configureMapSettings();

        zoomHandler.post(MoniterZoom);

        //TimeoutDestroyFiles destroy = new TimeoutDestroyFiles();
        //destroy.execute();
    }

    @Override
    protected void onDestroy() {
        Log.d("ONDESTROY", "Events");
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
        lm.removeUpdates(this);
    }

    //This class allows us to push things to the database easily and efficiently
    public static class Content {

        private double lon, lat;
        private long time;
        private String type, data, desc, user;
        public Content() {}
        public Content(String type, double lon, double lat, long time, String data, String desc, String user)
        {
            this.type = type;
            this.lon = lon;
            this.lat = lat;
            this.time = time;
            this.data = data;
            this.desc = desc;
            this.user = user;
        }

        public String getData() {
            return data;
        }

        public String getDesc() {
            return desc;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public long getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public String getUser() {
            return user;
        }

        @Override
        public String toString() {
            return "Content{Type: " + type + " Lon: " + lon + " Lat: " + lat + " Time: " + time + " Data: " + data + " Desc: " + desc + " User: " + user + "}";
        }
    }

    //This method will be called (probably from a Handler) to add Markers
    private void placeMarker(Content content, String uniqueID) {

        //This will be the code to add Markers to the Google Map
        LatLng pos = new LatLng(content.getLat(), content.getLon());
        String title = content.getType() + "_" + content.getTime() + "_" + content.getUser();
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        if(content.getType().equals("Picture"))
        {
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        }
        Marker marker = googleMap.addMarker(new MarkerOptions()
                        .icon(icon)
                        .title(title)
                        .position(pos)
        );
        markers.put(uniqueID, marker);
        uniqueIDs.put(title, uniqueID);
        descriptions.put(uniqueID, content.getDesc());

        Log.d("Descriptions: ", descriptions.toString());
        //As an alternative to when we click on a Marker, we could write the
        //  temp file now (since we already have the filename and the data)
    }

    //This method will be called (probably from a Handler) to remove Markers
    private void removeMarker(String uniqueID) {

        //Logic to make sure that it is safe to remove a marker (locks?)

        //Find marker in markers
        Marker marker = markers.get(uniqueID);
        String title = marker.getTitle();
        marker.remove();

        markers.remove(uniqueID);
        uniqueIDs.remove(title);
        descriptions.remove(uniqueID);

        //Remove tempfile (if any) associated with this marker
        String type = title.split("_")[0];
        if(type.equals("Video"))
        {
            title += ".mp4";
        }
        else
        {
            title += ".png";
        }
        RemoveFiles r = new RemoveFiles(title);
        r.execute();
    }

    private class RemoveFiles extends AsyncTask<Void, Void, Void> {

        private String file_to_delete;
        private File file;

        public RemoveFiles(String filename) {
            file_to_delete = filename;
            file = getContentFile(file_to_delete.split("_")[0] + "s", filename);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //We need to wait for the file to be not busy
            //  We can safely assume that it won't be being written, just watched/seen

            //We should be able to call

            while(file_to_delete.equals(current_content)) {}
            //Now it isn't being watched
            if(file.exists())
            {
                file.delete();
            }
            return null;
        }
    }

    /*private class TimeoutDestroyFiles extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //We should be able to sleep here
            //Thread.sleep(60*1000);
            ref.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    long secsEpoch = System.currentTimeMillis() / 1000;
                    final ArrayList<DataSnapshot> toBeRemoved = new ArrayList<DataSnapshot>();
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        long secsSinceEpochContent = snap.getValue(Content.class).getTime();
                        if (secsEpoch - secsSinceEpochContent >= EXPIRES_AFTER) //If it should be deleted
                        {
                            toBeRemoved.add(snap);
                        }
                    }
                    if (toBeRemoved.size() > 0) //There is an element to delete
                    {
                        ref.runTransaction(new Transaction.Handler()
                        {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData)
                            {
                                for (DataSnapshot snap : toBeRemoved)
                                {
                                    String key = snap.getKey();
                                    mutableData.child(key).setValue(null);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

                        }
                        });
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError)
                {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TimeoutDestroyFiles dest = new TimeoutDestroyFiles();
            dest.execute();
        }
    }
    */

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

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

}