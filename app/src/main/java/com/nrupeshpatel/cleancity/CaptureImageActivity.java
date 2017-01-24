/*
 * Copyright (c) 2017 Nrupesh Patel
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.nrupeshpatel.cleancity;

import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nrupeshpatel.cleancity.helper.Config;
import com.nrupeshpatel.cleancity.helper.ConnectionDetector;
import com.nrupeshpatel.cleancity.helper.PermissionUtils;
import com.nrupeshpatel.cleancity.helper.RequestHandler;
import com.nrupeshpatel.cleancity.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CaptureImageActivity extends AppCompatActivity {

    private boolean isGlide = true;
    private boolean isImageReady = false;
    private ImageView capturedImage;
    private String latitude;
    private String longitude;
    private String address;
    private String detail;
    private ProgressDialog loading;
    private SessionManager session;
    private String image = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        String realPath = getIntent().getStringExtra("realPath");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        session = new SessionManager(getApplicationContext());

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done);
        final EditText detailEt = (EditText) findViewById(R.id.detail);

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
                            detail = detailEt.getText().toString();

                            if (isGlide) {
                                image = getStringImage(scaleBitmapDown(
                                        ((GlideBitmapDrawable) capturedImage.getDrawable()).getBitmap(), 1500));
                            } else {
                                image = getStringImage(scaleBitmapDown(
                                        ((BitmapDrawable) capturedImage.getDrawable()).getBitmap(), 1500));
                            }

                            new GetLocation().execute();
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

    private class AddComplaint extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setTitle("Posting complaint...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loading.dismiss();
            finish();
        }

        @Override
        protected String doInBackground(String... params) {

            RequestHandler rh = new RequestHandler();
            HashMap<String, String> data = new HashMap<>();
            HashMap<String, String> user = session.getUserDetails();

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String formattedDate = df.format(c.getTime());

            data.put("email", user.get(SessionManager.KEY_EMAIL));
            data.put("address", address);
            data.put("detail", detail);
            data.put("date", formattedDate);
            data.put("image", image);
            return rh.sendPostRequest(Config.addComplaint, data);
        }
    }

    private class GetLocation extends AsyncTask<Bitmap, Void, String> {

        String JSON_STRING;
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(CaptureImageActivity.this, null, "Getting location...", true, true);
            loading.setCancelable(false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new AddComplaint().execute();
        }

        @Override
        protected String doInBackground(Bitmap... params) {

            RequestHandler rh = new RequestHandler();
            JSON_STRING = rh.sendGetRequest("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude);
            try {
                jsonObject = new JSONObject(JSON_STRING);
                JSONArray result = jsonObject.getJSONArray("results");
                JSONObject jo = result.getJSONObject(0);
                address = jo.getString("formatted_address");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
