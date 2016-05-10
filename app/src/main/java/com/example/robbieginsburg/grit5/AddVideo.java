package com.example.robbieginsburg.grit5;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddVideo extends android.support.v4.app.Fragment implements View.OnClickListener {

    private static final int VIDEO = 1;
    private AddMedia myParent;
    private Uri file_destination;

    public AddVideo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main = inflater.inflate(R.layout.fragment_add_video, container, false);

        Button add_video = (Button) main.findViewById(R.id.captureVideoButton);
        add_video.setOnClickListener(this);
        return main;
    }

    @Override
    public void onClick(View v) {
        Log.d("OnClick", "THIS WORKED");
        switch(v.getId())
        {
            case R.id.captureVideoButton:
                Log.d("OnClick", "VideoButton");
                file_destination = myParent.getFileLocation("Video");
                Intent video_Intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                video_Intent.putExtra(MediaStore.EXTRA_OUTPUT, file_destination);
                video_Intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10485760L); // 10 MB size limit
                startActivityForResult(video_Intent, VIDEO);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIDEO && resultCode == Activity.RESULT_OK)
        {
            //This is where we send the data back. This call will start the new Activity
            //  The 2nd parameter is true since this is a video
            myParent.pushToDataBase(file_destination, true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myParent = (AddMedia) context;
    }
}
