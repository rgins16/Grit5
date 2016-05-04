package com.example.robbieginsburg.grit5;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.Calendar;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Button maps;
    Intent homeScreen, events, socialMap, infoMap, phoneBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // disables the title showing the name of the app
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        maps = (Button) findViewById(R.id.mapsButton);
        maps.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // if the user clicked the events button, open the events map UI
            case R.id.mapsButton:
                Intent intent = new Intent(this, EventsActivity.class);
                startActivity(intent);
        }
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