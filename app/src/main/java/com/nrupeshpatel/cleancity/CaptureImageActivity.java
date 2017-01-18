package com.nrupeshpatel.cleancity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nrupeshpatel.cleancity.helper.ConnectionDetector;

import java.io.File;
import java.io.IOException;

public class CaptureImageActivity extends AppCompatActivity {

    private boolean isGlide = true;
    private boolean isImageReady = false;
    private ImageView capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        String realPath = getIntent().getStringExtra("realPath");

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.getBackground().setAlpha(0);

        capturedImage = (ImageView) findViewById(R.id.capturedImage);

        File file = new File(realPath);

        Glide.with(this)
                .load(file)
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        isImageReady = true;
                        return false;
                    }
                })
                .into(capturedImage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Uri.parse("file://" + Environment.getExternalStorageDirectory()).toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    // TODO Auto-generated method stub

                }
            });
        } else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageReady) {
                    try {
                        ConnectionDetector cd = new ConnectionDetector(CaptureImageActivity.this);
                        Boolean isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {

                            //callCloudVision(scaleBitmapDown(
                                    //((GlideBitmapDrawable) capturedImage.getDrawable()).getBitmap(), 1500));

                        } else {
                            Toast.makeText(CaptureImageActivity.this, "No Internet Connectivity!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) /*IOException*/ {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CaptureImageActivity.this, "Image resource not ready!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        if (id == R.id.action_rotate) {
            rotate(90);
        }

        return super.onOptionsItemSelected(item);
    }

    private void rotate(float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap toTransform;

        if (isGlide) {
            toTransform = ((GlideBitmapDrawable) capturedImage.getDrawable()).getBitmap();
            isGlide = false;
        } else {
            toTransform = ((BitmapDrawable) capturedImage.getDrawable()).getBitmap();
        }

        capturedImage.setImageBitmap(Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true));
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}
