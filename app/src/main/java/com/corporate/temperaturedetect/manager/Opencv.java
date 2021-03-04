package com.corporate.temperaturedetect.manager;

import android.content.Context;
import android.util.Log;

import com.corporate.temperaturedetect.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Opencv {
    private static final String TAG = "Opencv";

    private CascadeClassifier haarcascade_frontalface_alt2;
    private boolean statusEnableLibrary;
    private Context context;
    //Callback openCV library initialize
    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(context) {
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            if (status == LoaderCallbackInterface.SUCCESS) {
                statusEnableLibrary = true;
                haarcascade_frontalface_alt2 = createCascadeClassifierResources(R.raw.haarcascade_frontalface_alt2, "haarcascade_frontalface_alt2", ".xml", true);
            }
        }
    };

    public Opencv(Context context) {
        statusEnableLibrary = false;
        this.context = context;
        checkLibrary();
    }

    /**
     * Return CascadeClassifier face detector (haarcascade_frontalface_alt2)
     *
     * @return CascadeClassifier
     */
    public CascadeClassifier getFaceDetector() {
        return haarcascade_frontalface_alt2;
    }

    /**
     * Return true if library is loading
     *
     * @return boolean
     */
    public boolean isStatusEnableLibrary() {
        return statusEnableLibrary;
    }

    /**
     * Check if the library is enabled, if it is disabled call the asynchronous method for enabling
     */
    private void checkLibrary() {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Use method async load openCV library");
            //Initialize opencv library
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, mLoaderCallBack);
        } else {
            Log.i(TAG, "Opencv library load");
            statusEnableLibrary = true;
            haarcascade_frontalface_alt2 = createCascadeClassifierResources(R.raw.haarcascade_frontalface_alt2, "haarcascade_frontalface_alt2", ".xml", true);
        }
    }

    /**
     * Create CascadeClassifier loading haarcascade from Android Resources
     *
     * @return CascadeClassifier
     */
    private CascadeClassifier createCascadeClassifierResources(int resourceHaarcascadeXml, String name, String extension, boolean deleteDir) {
        final String TAG = "CreateCascadeClassifierResources";
        CascadeClassifier mJavaDetector;
        try {

            InputStream is = context.getResources().openRawResource(resourceHaarcascadeXml);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, name + extension);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.flush();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else {
                Log.i(TAG, "Loaded " + name + extension + " from " + mCascadeFile.getAbsolutePath());
            }

            if (deleteDir) {
                if (cascadeDir.delete())
                    Log.i(TAG, "Delete directory " + cascadeDir.toString());
            }

            return mJavaDetector;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            return null;
        }

    }
}
