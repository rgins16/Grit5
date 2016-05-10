package com.example.robbieginsburg.grit5;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;

public class AddMedia extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MediaInterface {

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook;

    ViewPager pager;
    TabLayout tabLayout;

    private String UMBC_username;
    private ArrayList<Intent> nextIntents;

    private long secsSinceEpoch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_media);
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

        Intent this_intent = getIntent();
        if(this_intent != null) UMBC_username = this_intent.getStringExtra("UMBC_Email");

        // sets all the intents for if a user clicks one of the buttons in the navbar
        homeScreen = new Intent(this, HomeScreen.class);
        upComing = new Intent(this, UpComing.class);
        happeningNow = new Intent(this, EventsActivity.class);
        infoMap = new Intent(this, InfoMaps.class);
        phoneBook = new Intent(this, PhoneBook.class);

        nextIntents = new ArrayList<>();
        nextIntents.add(homeScreen);
        nextIntents.add(upComing);
        nextIntents.add(happeningNow);
        nextIntents.add(infoMap);
        nextIntents.add(phoneBook);

        for(Intent next_intent : nextIntents)
        {
            next_intent.putExtra("UMBC_email", UMBC_username);
        }

        pager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        // Fragment manager to add fragment in viewpager we will pass object of Fragment manager to adpater class.
        FragmentManager manager=getSupportFragmentManager();

        //object of PagerAdapter passing fragment manager object as a parameter of constructor of PagerAdapter class.
        PagerAdapter adapter= new PageAdapter(manager);

        //set Adapter to view pager
        pager.setAdapter(adapter);

        //set tablayout with viewpager
        tabLayout.setupWithViewPager(pager);

        // adding functionality to tab and viewpager to manage each other when a page is changed or when a tab is selected
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //Setting tabs from adapter
        tabLayout.setTabsFromPagerAdapter(adapter);

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

    @Override
    public Uri getFileLocation(String type) {
        secsSinceEpoch = System.currentTimeMillis()/1000;
        String fileName = "" + secsSinceEpoch + "_" + UMBC_username;
        if(type.equals("Picture"))
        {
            fileName = "Picture_" + fileName + ".png";
        }
        else
        {
            fileName = "Video_" + fileName + ".mp4";
        }
        File file = getContentFile(type+"s", fileName);
        return Uri.fromFile(file);
    }

    @Override
    public void pushToDataBase(Uri content_file, boolean isVideo) {
        //This call will push the file to the database, and then return (back to eventsActivity)
        //  This simplifies the communication logic of the Intents
        upComing.setData(content_file);
        upComing.putExtra("IS_VIDEO", isVideo);
        upComing.putExtra("SECS_EPOCH", secsSinceEpoch);
        setResult(RESULT_OK, upComing);
        finish();

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
}
