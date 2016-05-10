package com.example.robbieginsburg.grit5;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class PhoneBook extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook, phone;

    Button info, undergradAdmissions, graduateAdmissions, profAdmissions, summerwinterAdmissions,
            alumniRelations, athletics, careerServices, financialAid, library, parkingServices, police,
            publicRelations, residentLife, shriverCenter, studentBusinessServices, trainingCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_book);
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

        // declares the phone intent
        phone = new Intent(Intent.ACTION_DIAL);

        info = (Button) findViewById(R.id.informationButton);
        assert info != null;
        info.setOnClickListener(this);

        undergradAdmissions = (Button) findViewById(R.id.undergradAdmissionButton);
        assert undergradAdmissions != null;
        undergradAdmissions.setOnClickListener(this);

        graduateAdmissions = (Button) findViewById(R.id.gradAdmissionButton);
        assert graduateAdmissions != null;
        graduateAdmissions.setOnClickListener(this);

        profAdmissions = (Button) findViewById(R.id.admissionsProfStudiesButton);
        assert profAdmissions != null;
        profAdmissions.setOnClickListener(this);

        summerwinterAdmissions = (Button) findViewById(R.id.admissionsSummerWinterButton);
        assert summerwinterAdmissions != null;
        summerwinterAdmissions.setOnClickListener(this);

        alumniRelations = (Button) findViewById(R.id.alumniRelationsButton);
        assert alumniRelations != null;
        alumniRelations.setOnClickListener(this);

        athletics = (Button) findViewById(R.id.athleticsButton);
        assert athletics != null;
        athletics.setOnClickListener(this);

        careerServices = (Button) findViewById(R.id.careerServicesButton);
        assert careerServices != null;
        careerServices.setOnClickListener(this);

        financialAid = (Button) findViewById(R.id.financialAidButton);
        assert financialAid != null;
        financialAid.setOnClickListener(this);

        library = (Button) findViewById(R.id.libraryButton);
        assert library != null;
        library.setOnClickListener(this);

        parkingServices = (Button) findViewById(R.id.parkingServices);
        assert parkingServices != null;
        parkingServices.setOnClickListener(this);

        police = (Button) findViewById(R.id.policeButton);
        assert police != null;
        police.setOnClickListener(this);

        publicRelations = (Button) findViewById(R.id.publicRelationsButton);
        assert publicRelations != null;
        publicRelations.setOnClickListener(this);

        residentLife = (Button) findViewById(R.id.resLifeButton);
        assert residentLife != null;
        residentLife.setOnClickListener(this);

        shriverCenter = (Button) findViewById(R.id.shriverCenterButton);
        assert shriverCenter != null;
        shriverCenter.setOnClickListener(this);

        studentBusinessServices = (Button) findViewById(R.id.studentBusinessButton);
        assert studentBusinessServices != null;
        studentBusinessServices.setOnClickListener(this);

        trainingCenter = (Button) findViewById(R.id.trainingCenterButton);
        assert trainingCenter != null;
        trainingCenter.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.informationButton:
                phone.setData(Uri.parse("tel:4104551000"));
                startActivity(phone);
                break;
            case R.id.undergradAdmissionButton:
                phone.setData(Uri.parse("tel:4104552291"));
                startActivity(phone);
                break;
            case R.id.gradAdmissionButton:
                phone.setData(Uri.parse("tel:4104552537"));
                startActivity(phone);
                break;
            case R.id.admissionsProfStudiesButton:
                phone.setData(Uri.parse("tel:4104552336"));
                startActivity(phone);
                break;
            case R.id.admissionsSummerWinterButton:
                phone.setData(Uri.parse("tel:4104552335"));
                startActivity(phone);
                break;
            case R.id.alumniRelationsButton:
                phone.setData(Uri.parse("tel:4104552904"));
                startActivity(phone);
                break;
            case R.id.athleticsButton:
                phone.setData(Uri.parse("tel:4104552126"));
                startActivity(phone);
                break;
            case R.id.careerServicesButton:
                phone.setData(Uri.parse("tel:4104552216"));
                startActivity(phone);
                break;
            case R.id.financialAidButton:
                phone.setData(Uri.parse("tel:4104552387"));
                startActivity(phone);
                break;
            case R.id.libraryButton:
                phone.setData(Uri.parse("tel:4104552233"));
                startActivity(phone);
                break;
            case R.id.parkingServices:
                phone.setData(Uri.parse("tel:4104552551"));
                startActivity(phone);
                break;
            case R.id.policeButton:
                phone.setData(Uri.parse("tel:4104553133"));
                startActivity(phone);
                break;
            case R.id.publicRelationsButton:
                phone.setData(Uri.parse("tel:4104552902"));
                startActivity(phone);
                break;
            case R.id.resLifeButton:
                phone.setData(Uri.parse("tel:4104552591"));
                startActivity(phone);
                break;
            case R.id.shriverCenterButton:
                phone.setData(Uri.parse("tel:4104552493"));
                startActivity(phone);
                break;
            case R.id.studentBusinessButton:
                phone.setData(Uri.parse("tel:4104552288"));
                startActivity(phone);
                break;
            case R.id.trainingCenterButton:
                phone.setData(Uri.parse("tel:4435435400"));
                startActivity(phone);
                break;
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
