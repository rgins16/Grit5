package com.example.robbieginsburg.grit5;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class UpComing extends AppCompatActivity /*implements NavigationView.OnNavigationItemSelectedListener*/{

    Intent homeScreen, upComing, happeningNow, infoMap, phoneBook;

    private static final String DEBUG_TAG = "HttpExample";
    private TextView urlText;
    private TextView textView;

    ProgressDialog mProgress;
    private ArrayList<String> listItems;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private String today;

    private String umbcId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_coming);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // disables the title showing the name of the app
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        phoneBook = new Intent(this, PhoneBook.class);*/

        // specifies linear layout for the activity
        // defines the layout details
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        // creates a viewgroup to hold all the events
        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // creates a calendar object to get the current date
        // this will be displayed at the top of the page
        // to let the user know what day it is
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);

        // saves the date to the textview and displays it
        urlText = new TextView(this);
        urlText.setLayoutParams(tlp);
        urlText.setPadding(16, 16, 16, 16);
        activityLayout.addView(urlText);
        urlText.setText("http://my.umbc.edu/events/" + year + "/" + month + "/" + day);

        // saves the date for later
        today = ""+year+"-"+month+"-"+day;

        // sets a new text view to empty
        textView = new TextView(this);
        textView.setLayoutParams(tlp);
        textView.setPadding(16, 16, 16, 16);
        activityLayout.addView(textView);
        textView.setText("");

        // sets height and width
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;


        // sets up a listview in the view group
        listView = new ListView(this);
        tlp.height = (int)(height * 0.7);
        listView.setLayoutParams(tlp);
        listView.setPadding(16, 16, 16, 16);
        activityLayout.addView(listView);

        //
        Intent intent = getIntent();
        umbcId = intent.getStringExtra("umbcId");

        // initialises the list of events, and linked lists that are each event
        // the list will hold all of the linked lists
        listItems=new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(adapter);

        // sets the view to the one that is created above
        setContentView(activityLayout);

        //textView.setText(date.toString());
        String stringUrl = urlText.getText().toString();
        urlText.setEnabled(false);

        // sets the loading text
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading the events ...");

        // gets the active network connection info
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // if there is an active network connection
        if (networkInfo != null && networkInfo.isConnected()) {
            // call the download webpage async task
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            // otherwise state there is no available connection
            textView.setText("No network connection available.");
        }

        // on click listener for clicking an item on the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // start the intent for the popup that appears when an event is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = ((TextView) view).getText().toString();
                Intent intent = new Intent(UpComing.this, PopUpUmbcEvent.class);
                intent.putExtra("item", item);
                intent.putExtra("today", today);
                startActivity(intent);

            }
        });


        // button for viewing itinerary
        Button buttonMyCalendar = new Button(this);
        buttonMyCalendar.setText("My Itinerary");
        buttonMyCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyCalendar.class);
                //intent.putExtra("umbcId",umbcId);
                startActivity(intent);
            }
        });
        activityLayout.addView(buttonMyCalendar);

        // button for going back
        Button buttonGoBack = new Button(this);
        buttonGoBack.setText("Back");
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activityLayout.addView(buttonGoBack);

    }


    public String processListItem(String eventItem){
        String []fields = eventItem.split("\\|");
        String newItem = "";
                /*for (String field:fields){
                    newItem += field;
                }*/
        String noon = "Noon";
        if(fields[0].trim().equals(noon))
            fields[0]="12:00 PM";
        if(fields[1].trim().equals(noon))
            fields[1]="12:00 PM";


        if(!fields[0].trim().contains(":")){
            fields[0] = fields[0].replace(" PM",":00 PM");
            fields[0] = fields[0].replace(" AM",":00 AM");
        }
        if(!fields[1].trim().contains(":")){
            fields[1] = fields[1].replace(" PM",":00 PM");
            fields[1] = fields[1].replace(" AM",":00 AM");
        }

        newItem += "Title      : "+fields[5]+"\n";
        newItem += "Starts at  : "+fields[0]+"\n";
        newItem += "Ends at    : "+fields[1]+"\n";
        newItem += "Location   : "+fields[2]+"\n";
        newItem += "Description: "+fields[3]+"\n";
        newItem += "Web Page   : "+fields[4]+"\n";
        return newItem;
    }

    // async task for downloading the events webpage from umbc and reading it
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute()
            // the parameter is the url which includes the current date
            try {
                // gets the content to be displayed to the user
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }


        // while the content is being fetched and converted to readable format, show the user
        // a loading message
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.show();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            mProgress.hide();
            textView.setText("The events happening today are:");
            String []eventItems =  result.split("\n");
            for (String eventItem: eventItems){
                // makes the event look nice
                String newItem = processListItem(eventItem);
                adapter.add(newItem);
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {

            InputStream is = null;

            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 50000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(20000 /* milliseconds */);
                conn.setConnectTimeout(25000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);

                // converts the string into the readable object that will be displayed to the user
                String result = parseXmlString(contentAsString);
                return result;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // takes the string from the input stream from the website, and formats it so
        // it will look nice to the user
        public String parseXmlString(String html){
            //textView.setText(str);

            html = html.replaceAll("event-item content-item", "event-item-content-item");
            String hrefUrl = "http://my.umbc.edu/";
            //String result="The events happening today are:";
            String parsedString = "";

            try {
                Document doc = Jsoup.parse(html);
                Elements elements = doc.getElementsByAttributeValue("class", "event-item-content-item");

                for (Element element: elements)
                {
                    Elements starts_at = element.getElementsByAttributeValue("class", "starts-at");
                    Elements ends_at = element.getElementsByAttributeValue("class", "ends-at");
                    Elements locations = element.getElementsByAttributeValue("class", "location");
                    Elements blurbs = element.getElementsByAttributeValue("class", "blurb");
                    Elements titles = element.getElementsByAttributeValue("class", "title");


                    if(starts_at.size()>0)
                        for(Element elem:starts_at)
                            parsedString += elem.ownText() +" |";
                    else
                        parsedString += "-----|";
                    if(ends_at.size()>0)
                        for(Element elem:ends_at)
                            parsedString += elem.ownText().replace("to","") +" |";
                    else
                        parsedString += "-----|";
                    if(locations.size()>0)
                        for(Element elem:locations)
                            parsedString += elem.ownText() +" |";
                    else
                        parsedString += "-----|";
                    if(blurbs.size()>0)
                        for(Element elem:blurbs)
                            parsedString += elem.ownText() +" |";
                    else
                        parsedString += "-----|";
                    if(titles.size()>0)
                        for(Element elem:titles)
                        {
                            Elements hrefs = elem.getElementsByAttribute("href");
                            if (hrefs.size()>0){
                                for (Element href:hrefs)
                                {
                                    parsedString += hrefUrl+href.attr("href") +" |";
                                }
                            }
                            else
                                parsedString += "-----|";
                            parsedString += elem.text();
                        }
                    else
                        parsedString += "-----";

                    parsedString += "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return parsedString;

        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            byte[] bytes = new byte[len];
            StringBuilder x = new StringBuilder();

            int numRead = 0;
            while ((numRead = stream.read(bytes)) >= 0) {
                x.append(new String(bytes, 0, numRead));
            }

            return new String(x);
        }
    }

    /*@Override
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
    }*/
}

