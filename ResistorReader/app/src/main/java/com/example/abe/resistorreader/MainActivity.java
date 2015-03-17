package com.example.abe.resistorreader;
/*
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends Activity {

    private static String logtag = "CameraApp";
    private static int TAKE_PICTURE = 1;
    final int PIC_CROP = 1;
    private Uri picUri;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button resultsButton = (Button) findViewById(R.id.results_btn);
        cameraButton.setOnClickListener(cameraListener);
        resultsButton.setOnClickListener(resultsListener);

    }

    private View.OnClickListener resultsListener;

    {
        resultsListener = new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.activity_results);

            }

        };
    }

    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            performCrop();
            takePhoto(v);

        }
    };

    private void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
        picUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, requestCode, intent);

        if (requestCode == Activity.RESULT_OK) {
            Uri selectedImage = picUri;
            getContentResolver().notifyChange(selectedImage, null);

            ImageView imageView = (ImageView) findViewById(R.id.image_camera);
            ContentResolver cr = getContentResolver();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(MainActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(logtag, e.toString());
            }
        }
    }


    private void performCrop(Uri picUri){
        //take care of exceptions
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        //respond to users whose devices do not support the crop action
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
*/

//ADD PACKAGE HERE

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends Activity implements OnClickListener {

    //keep track of camera capture intent
    final int CAMERA_CAPTURE = 1;
    //keep track of cropping intent
    final int PIC_CROP = 2;
    //captured picture uri
    private Uri picUri;
    //declares the bitmap to store the pic
    Bitmap thePic;
    //The image we want to display
    Bitmap displayImage;
    ImageView picView;
    //Load OpenCV libraries.
    static {
        if(!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
        } else {
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button resultsButton = (Button) findViewById(R.id.results_btn);
        cameraButton.setOnClickListener(this);
        resultsButton.setOnClickListener(this);

    }

    /**
     * Click method to handle user pressing button to launch camera
     */
    public void onClick(View v) {
        if (v.getId() == R.id.button_camera) {
            try {
                //use standard intent to capture an image
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //we will handle the returned data in onActivityResult
                startActivityForResult(captureIntent, CAMERA_CAPTURE);
            }
            catch(ActivityNotFoundException anfe){
                //display an error message
                String errorMessage = "Whoops - your device doesn't support capturing images!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /**
     * Handle user returning from both capturing and cropping the image
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if(requestCode == CAMERA_CAPTURE){
                //get the Uri for the captured image
                picUri = data.getData();
                Log.v("CSUSB", data.getDataString());
                //carry out the crop operation
                performCrop();
                Log.v("CSUSB", data.getDataString());
                //colorDetect();
            }
            //user is returning from cropping the image
            else if(requestCode == PIC_CROP){
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                thePic = extras.getParcelable("data");
                //change content view
                setContentView(R.layout.activity_results);
                //display the returned cropped image
                picView =  (ImageView)findViewById(R.id.resultsView);
                colorDetect();
                picView.setImageBitmap(displayImage);

            }
        }
    }

    /**
     * Helper method to carry out crop operation
     */
    private void performCrop(){
        //take care of exceptions
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        //respond to users whose devices do not support the crop action
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void colorDetect() {
        Mat temp = new Mat (thePic.getWidth(), thePic.getHeight(), CvType.CV_8UC1);
        Mat PicHSV = new Mat();
        displayImage = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
        //Bitmap bitmapPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), temp);
        Utils.bitmapToMat(thePic, temp);


        //cvtColor(temp, PicHSV, COLOR_RGB2GRAY);
        double iLastX = -1;
        double iLastY = -1;


        Mat imgHSV = new Mat();

        cvtColor(temp, imgHSV, COLOR_BGR2HSV);

        //Search for colors between these HSV values.
        Core.inRange(imgHSV, new Scalar(40,57,184), new Scalar(179,231,255), imgHSV );

        erode(imgHSV,imgHSV, getStructuringElement(MORPH_ELLIPSE, new Size(5,5)));
        dilate(imgHSV, imgHSV, getStructuringElement(MORPH_ELLIPSE, new Size(5,5)));

        dilate(imgHSV, imgHSV, getStructuringElement(MORPH_ELLIPSE, new Size(5,5)));
        erode(imgHSV,imgHSV, getStructuringElement(MORPH_ELLIPSE, new Size(5,5)));


        Moments oMoments = Imgproc.moments(imgHSV);
        double dM01 = oMoments.get_m01();
        double dM10 = oMoments.get_m10();
        double dArea = oMoments.get_m00();

        //  if(dArea > 10000)
        // {
        double posX = dM10 / dArea;
        double posY = dM01 / dArea;

        if(iLastX >= 0 && iLastY >= 0 && posX >= 0 && posY >= 0)
        {
            Core.line(temp,new Point(posX, posY), new Point(iLastX, iLastY), new Scalar(255,255,100), 20);
        }
        iLastX = posX;
        iLastY = posY;
        //}

        //     Core.line(temp,new Point(100, 100), new Point(200, 200), new Scalar(255,255,100), 20);
        Core.line(temp,new Point(posX, posY), new Point(iLastX, iLastY), new Scalar(255,255,100), 20);

        Utils.matToBitmap(temp, displayImage);
        picView.setImageBitmap(displayImage);

    }
}