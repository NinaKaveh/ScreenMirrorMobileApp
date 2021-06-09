package com.example.screenmirrormobileapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;

public class PhoneScreen extends bluetoothConnect {

    ImageReader imageReader;
    Handler handler = new Handler();
    VirtualDisplay virtualDisplay;

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void createImage() {
        // Get the window size (width, height, metrics and density)
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        final int width = size.x;
        final int height = size.y;
        int density = metrics.densityDpi;

        // create a reader for image
        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);

        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

        // creation of the capture
        virtualDisplay = mediaProjection.createVirtualDisplay("screen capture", width, height, density, flags, imageReader.getSurface(), null, handler);

        // create bitmap (create a file screen.jpg)
        imageReader.setOnImageAvailableListener(reader -> {
            Image image;
            FileOutputStream outputStream = null;
            Bitmap bitmap;

            try {
                image = imageReader.acquireLatestImage();
                outputStream = new FileOutputStream(getFilesDir() + "/screen.jpg");
                final Image.Plane[] planes = image.getPlanes();
                final Buffer buffer = planes[0].getBuffer().rewind();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap.compress(Bitmap.CompressFormat.PNG, 5, outputStream);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream!=null) {
                    try {
                        outputStream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }, handler);
    }
}
