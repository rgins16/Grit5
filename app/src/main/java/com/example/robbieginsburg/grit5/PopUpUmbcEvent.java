package com.example.robbieginsburg.grit5;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopUpUmbcEvent extends AppCompatActivity {

    private String item = "";
    private String today = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_umbc_event);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .7));
        final TextView textView = (TextView) findViewById(R.id.textView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            item = extras.getString("item");
            today = extras.getString("today");
            textView.setText(item);
        }

        Button buttonGoToWebPage = (Button) findViewById(R.id.buttonGoToWebPage);
        buttonGoToWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] fields = item.split("\n");
                String address = fields[5].trim().replace("Web Page   : ", "");
                //textView.setText(address);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(address));
                startActivity(i);

            }
        });

        Button buttonGoBack = (Button)findViewById(R.id.buttonGoBack);
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button buttonAddToCalendar = (Button) findViewById(R.id.buttonAddToCalendar);
        buttonAddToCalendar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String[] fields = item.split("\n");

                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setData(CalendarContract.Events.CONTENT_URI);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d-h:mm aa");
                        try {
                            String start = today + "-" + fields[1].replace("Starts at  : ", "");
                            Date date = dateFormat.parse(start);
                            //Toast.makeText(getBaseContext(), date.toString(), Toast.LENGTH_LONG).show();
                            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.getTime());
                            String end = today + "-" + fields[2].replace("Ends at    : ", "");
                            Date date2 = dateFormat.parse(end);
                            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date2.getTime());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        intent.putExtra(CalendarContract.Events.DESCRIPTION, fields[4].replace("Description: ", ""));
                        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, fields[3].replace("Location   : ", ""));
                        intent.putExtra(CalendarContract.Events.TITLE, fields[0].replace("Title      : ", ""));
                        startActivity(intent);
                        //Toast.makeText(getBaseContext(), "event added to calendar", Toast.LENGTH_LONG).show();


                    }
                }
        );
    }

}
