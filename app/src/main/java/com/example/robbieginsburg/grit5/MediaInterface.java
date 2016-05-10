package com.example.robbieginsburg.grit5;

import android.net.Uri;

/**
 * Created by Leslie on 5/9/2016.
 */
public interface MediaInterface {
    /*
        There are methods here to help the fragments communicate with the Activity
     */
    Uri getFileLocation(String type);
    void pushToDataBase(Uri content_file, boolean isVideo);
}
