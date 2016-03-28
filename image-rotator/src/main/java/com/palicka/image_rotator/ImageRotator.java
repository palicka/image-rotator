package com.palicka.image_rotator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by pavol fogas on 26/03/16.
 */
public class ImageRotator {

    private float angle;
    private float factor;
    private double beta;
    private double ro;
    private Bitmap bitmap;
    private Canvas canvas;
    private static ImageRotator imageRotator;

    public static ImageRotator getInstance() {
        if (imageRotator == null)
            imageRotator = new ImageRotator();
        return imageRotator;
    }

    private ImageRotator() {

    }

    public Bitmap init(Bitmap inputBitmap) {

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(), inputBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(outputBitmap);
        canvas.drawBitmap(inputBitmap, 0, 0, null);
        this.bitmap = inputBitmap;
        return outputBitmap;
    }

    public void rotate(float angle) {

        if (angle > 45)
            angle = 45;
        if (angle < -45)
            angle = -45;
        this.angle = angle;

        double viewRectangleBiggerSide;
        double viewRectangleSmallerSide;
        boolean isLandscape = bitmap.getWidth() > bitmap.getHeight();
        if (isLandscape) {
            viewRectangleBiggerSide = bitmap.getWidth();
            viewRectangleSmallerSide = bitmap.getHeight();
        } else {
            viewRectangleBiggerSide = bitmap.getHeight();
            viewRectangleSmallerSide = bitmap.getWidth();
        }
        double c = Math.sqrt(viewRectangleBiggerSide * viewRectangleBiggerSide + viewRectangleSmallerSide * viewRectangleSmallerSide);
        beta = Math.toDegrees(Math.atan(viewRectangleBiggerSide / viewRectangleSmallerSide));
        double epsilon = 90 - beta;
        if (angle < 0) {
            ro = 90 - (epsilon + (-1) * angle);
        } else {
            ro = 90 - (epsilon + angle);
        }
        double x = Math.cos(Math.toRadians(ro)) * c;
        if (isLandscape) {
            factor = (float) (x / canvas.getHeight());
        } else {
            factor = (float) (x / canvas.getWidth());
        }
        Matrix matrix = new Matrix();
        matrix.postScale(factor, factor, canvas.getWidth() / 2, canvas.getHeight() / 2);
        matrix.postRotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.drawBitmap(bitmap, matrix, null);
    }

    public Bitmap rotateOriginal(Bitmap bitmap) {

        Bitmap full;
        Bitmap resultBitmap;
        double fullB = 0;
        double fullA = 0;

        full = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasFull = new Canvas(full);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(angle, canvasFull.getWidth() / 2, canvasFull.getHeight() / 2);
        canvasFull.drawBitmap(bitmap, matrix, null);
        boolean isLandscape = bitmap.getWidth() > bitmap.getHeight();

        double cosRo = Math.cos(Math.toRadians(ro));
        double fullC;
        if (isLandscape) {
            fullC = canvasFull.getHeight() / cosRo;
        } else {
            fullC = canvasFull.getWidth() / cosRo;
        }
        double cosBeta = Math.cos(Math.toRadians(beta));
        if (isLandscape) {
            fullB = cosBeta * fullC;
            fullA = Math.sqrt(fullC * fullC - fullB * fullB);
            if (fullB > canvasFull.getHeight())
                fullB = canvasFull.getHeight();
            if (fullA > canvasFull.getWidth())
                fullA = canvasFull.getWidth();
        } else {
            fullA = cosBeta * fullC;
            fullB = Math.sqrt(fullC * fullC - fullA * fullA);
            if (fullA > canvasFull.getWidth())
                fullA = canvasFull.getWidth();
            if (fullB > canvasFull.getHeight())
                fullB = canvasFull.getHeight();
        }

        float newXPos = (float) (canvasFull.getWidth() / 2 - fullA / 2);
        float newYPos = (float) (canvasFull.getHeight() / 2 - fullB / 2);

        if (newXPos < 0)
            newXPos = 0;
        if (newYPos < 0)
            newYPos = 0;

        int[] pixels = new int[(int) fullA * (int) fullB];
        full.getPixels(pixels, 0, (int) fullA, (int) newXPos, (int) newYPos, (int) fullA, (int) fullB);

        resultBitmap = Bitmap.createBitmap((int) fullA, (int) fullB, Bitmap.Config.ARGB_8888);
        resultBitmap.setPixels(pixels, 0, (int) fullA, 0, 0, (int) fullA, (int) fullB);
        return resultBitmap;
    }

    public boolean needToBeResized(Bitmap inputBitmap) {
        return (inputBitmap.getWidth() > 4096 || inputBitmap.getHeight() > 4096);
    }

    public Bitmap resizeToFitCanvas(Bitmap bitmap) {
        Bitmap resizedBitmap;
        float multFactor;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        if (originalHeight > originalWidth) {
            newHeight = 4096;
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if (originalWidth > originalHeight) {
            newWidth = 4096;
            multFactor = (float) originalHeight / (float) originalWidth;
            newHeight = (int) (newWidth * multFactor);
        } else if (originalHeight == originalWidth) {
            newHeight = 4096;
            newWidth = 4096;
        }
        if (originalWidth > newWidth) {
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        } else {
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        bitmap.recycle();
        return resizedBitmap;
    }

}
