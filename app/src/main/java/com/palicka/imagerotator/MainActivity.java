package com.palicka.imagerotator;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.palicka.image_rotator.ImageRotator;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap originalBitmap;
    public float angle = 0;
    boolean isPortrait;
    int screenWidth, screenHeight;
    Bitmap scaledBitmap;
    private ImageRotator imageRotator;
    private Bitmap outputBitmap;
    private SeekBar rotationSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageview);
        rotationSeekbar = (SeekBar) findViewById(R.id.seekbar_rotate);
        rotationSeekbar.setProgress(45);

        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        imageView.setScaleType(ImageView.ScaleType.CENTER);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;

        //Get ImageRotator instance
        imageRotator = ImageRotator.getInstance();
        try {
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
            //Check if input bitmap needs to be resized to fit canvas
            if(imageRotator.needToBeResized(originalBitmap))
                //If so, resize it
                originalBitmap = imageRotator.resizeToFitCanvas(originalBitmap);
            //You should create scaled version of your original bitmap(if it's large) that will be shown to user.
            //It will be faster and smoother.
            scaledBitmap = resizeBitmapToFitScreen(originalBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Init image rotator
        Bitmap bitmapToBeRotated = imageRotator.init(scaledBitmap);
        //Set bitmap that will be rotated to your image view
        imageView.setImageBitmap(bitmapToBeRotated);

        rotationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle = progress - 45;
                //rotate image
                imageRotator.rotate(angle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // get rotated and cropped bitmap from original bitmap.
        // Note: do not execute this in the main thread. Use async task for example.
       // outputBitmap = imageRotator.rotateOriginal(originalBitmap);

    }


    public Bitmap resizeBitmapToFitScreen(Bitmap bitmap) {
        Bitmap resizedBitmap;
        float multFactor;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        if (isPortrait) {
            if (originalHeight > originalWidth) {
                newHeight = (int) screenHeight / 2;
                multFactor = (float) originalWidth / (float) originalHeight;
                newWidth = (int) (newHeight * multFactor);
            } else if (originalWidth > originalHeight) {
                newWidth = (int) screenWidth;
                multFactor = (float) originalHeight / (float) originalWidth;
                newHeight = (int) (newWidth * multFactor);
            } else if (originalHeight == originalWidth) {
                if (screenHeight < screenWidth) {
                    newHeight = (int) screenHeight;
                    newWidth = (int) screenHeight;
                } else {
                    newHeight = (int) screenWidth;
                    newWidth = (int) screenWidth;
                }
            }
        } else {
            newHeight = screenHeight / 2;
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        }
        if (originalWidth > newWidth) {
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        } else {
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return resizedBitmap;
    }


}
