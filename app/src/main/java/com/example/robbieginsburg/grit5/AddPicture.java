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
public class AddPicture extends android.support.v4.app.Fragment implements View.OnClickListener {

    private static final int CAMERA = 1;
    private AddMedia myParent;
    private Uri file_destination;

    public AddPicture() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main = inflater.inflate(R.layout.fragment_add_picture, container, false);

        Button add_picture = (Button) main.findViewById(R.id.capturePictureButton);
        add_picture.setOnClickListener(this);
        return main;
    }

    @Override
    public void onClick(View v) {
        Log.d("OnClick", "THIS WORKED");
        switch(v.getId())
        {
            case R.id.capturePictureButton:
                Log.d("OnClick", "PictureButton");
                file_destination = myParent.getFileLocation("Picture");
                Intent camera_Intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera_Intent.putExtra(MediaStore.EXTRA_OUTPUT, file_destination);
                startActivityForResult(camera_Intent, CAMERA);
                //Here we will start the camera Intent
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA && resultCode == Activity.RESULT_OK)
        {
            //This is where we send the data back. This call will start the new Activity
            //  the 2nd parameter is false since this is not a video
            myParent.pushToDataBase(file_destination, false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myParent = (AddMedia) context;
    }
}
