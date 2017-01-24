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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.nrupeshpatel.cleancity.fragments.HomeFragment;
import com.nrupeshpatel.cleancity.fragments.PendingFragment;
import com.nrupeshpatel.cleancity.fragments.SolvedFragment;
import com.nrupeshpatel.cleancity.fragments.StarredFragment;
import com.nrupeshpatel.cleancity.helper.ConnectionDetector;
import com.nrupeshpatel.cleancity.helper.PermissionUtils;
import com.nrupeshpatel.cleancity.helper.PrefManager;
import com.nrupeshpatel.cleancity.helper.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener, PendingFragment.OnFragmentInteractionListener, SolvedFragment.OnFragmentInteractionListener, StarredFragment.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private LocationRequest mLocationRequest;
    String longitude, latitude;

    private TextView toolbarTitle;
    private LocationManager locationManager;
    private PrefManager prefManager;
    private GoogleApiClient mGoogleApiClientAuth;
    private SessionManager session;
    private String realPath = null;
    public static final int CAMERA_PERMISSIONS_REQUEST = 1;
    public static final int CAMERA_IMAGE_REQUEST = 2;
    private TabLayout tabLayout;
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private int[] tabIconsInactive = {
            R.drawable.ic_home_tab_inactive,
            R.drawable.ic_pending_tab_inactive,
            R.drawable.ic_solved_tab_inactive,
            R.drawable.ic_star_tab_inactive
    };
    private int[] tabIconsActive = {
            R.drawable.ic_home_tab,
            R.drawable.ic_pending_tab,
            R.drawable.ic_solved_tab,
            R.drawable.ic_star_tab
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("realPath", realPath);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        realPath = savedInstanceState.getString("realPath");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        session = new SessionManager(getApplicationContext());

        CircleImageView toolbarLogo = (CircleImageView) findViewById(R.id.toolbarLogo);
        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClientAuth = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        toolbarTitle.setText("Home");

        prefManager = new PrefManager(this);

        if (prefManager.isFirstTimeLaunch()) {
            showAssistView();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startCamera();
            }
        });

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        viewPager.setOffscreenPageLimit(4);

        HashMap<String, String> user = session.getUserDetails();

        Bitmap bitmap = getImage(user.get(SessionManager.KEY_PROFILE));
        toolbarLogo.setImageBitmap(bitmap);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Enable Location")
                .setCancelable(false)
                .setMessage(getString(R.string.location_alert))
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIconsActive[0]);
        tabLayout.getTabAt(1).setIcon(tabIconsInactive[1]);
        tabLayout.getTabAt(2).setIcon(tabIconsInactive[2]);
        tabLayout.getTabAt(3).setIcon(tabIconsInactive[3]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new PendingFragment(), "Pending");
        adapter.addFragment(new SolvedFragment(), "Solved");
        adapter.addFragment(new StarredFragment(), "Starred");
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbarTitle.setText(mFragmentTitleList.get(position));
                tabLayout.getTabAt(position).setIcon(tabIconsActive[position]);
                for (int i = 0; i < 4; i++) {
                    if (i != position)
                        tabLayout.getTabAt(i).setIcon(tabIconsInactive[i]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return mFragmentTitleList.get(position);
            return null;
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA)) {
            if (isLocationEnabled()) {
                findLocation();
                Uri mHighQualityImageUri = generateTimeStampPhotoFileUri();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mHighQualityImageUri);
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
            } else {
                showAlert();
            }
        }
    }

    private Uri generateTimeStampPhotoFileUri() {

        android.net.Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null) {
            Time t = new Time();
            t.setToNow();
            File photoFile = new File(outputDir, System.currentTimeMillis()
                    + ".png");
            photoFileUri = android.net.Uri.fromFile(photoFile);
            realPath = photoFile.toString();
        }
        return photoFileUri;
    }

    private File getPhotoDirectory() {
        File outputDir = null;
        String externalStorageStagte = Environment.getExternalStorageState();
        if (externalStorageStagte.equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(photoDir, "CleanCity");
            if (!outputDir.exists())
                if (!outputDir.mkdirs()) {
                    Toast.makeText(
                            this,
                            "Error Creating Directory "
                                    + outputDir.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                    outputDir = null;
                }
        }
        return outputDir;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {

            Intent i = new Intent(this, CaptureImageActivity.class);
            i.putExtra("realPath", realPath);
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            startActivity(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            startCamera();
        }
    }

    public void showAssistView() {
        prefManager.setFirstTimeLaunch(false);

        new MaterialTapTargetPrompt.Builder(MainActivity.this)
                .setTarget(findViewById(R.id.fab))
                .setPrimaryText("Send your first complaint")
                .setSecondaryText("Tap the camera to start capturing your first complaint")
                .setBackgroundColour(getResources().getColor(R.color.colorPrimary))
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                        //Do something such as storing a value so that this prompt is never shown again
                        if (!tappedTarget) {
                            final MaterialTapTargetPrompt.Builder tapTargetPromptBuilder = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                                    .setPrimaryText("Just how you want it")
                                    .setSecondaryText("Tap the menu icon to switch accounts, change settings & more")
                                    .setBackgroundColour(getResources().getColor(R.color.colorPrimary))
                                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                                    .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                                    .setIcon(android.R.color.transparent)
                                    .setIconDrawableColourFilter(getResources().getColor(R.color.colorPrimary));
                            final CircleImageView tb = (CircleImageView) MainActivity.this.findViewById(R.id.toolbarLogo);
                            tapTargetPromptBuilder.setTarget(tb);

                            tapTargetPromptBuilder.setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                                @Override
                                public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                                    //Do something such as storing a value so that this prompt is never shown again
                                    if (!tappedTarget) {
                                        new MaterialTapTargetPrompt.Builder(MainActivity.this)
                                                .setPrimaryText("Logout from the app")
                                                .setSecondaryText("Tap the power button to logout current user")
                                                .setBackgroundColour(getResources().getColor(R.color.colorPrimary))
                                                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                                                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                                                .setIcon(R.drawable.ic_logout_actionbar)
                                                .setIconDrawableColourFilter(getResources().getColor(R.color.colorPrimary))
                                                .setTarget(R.id.action_logout)
                                                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                                                    @Override
                                                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                                                    }

                                                    @Override
                                                    public void onHidePromptComplete() {

                                                    }
                                                })
                                                .show();
                                    }
                                }

                                @Override
                                public void onHidePromptComplete() {

                                }
                            });
                            tapTargetPromptBuilder.show();
                        }
                    }

                    @Override
                    public void onHidePromptComplete() {

                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Auth.GoogleSignInApi.signOut(mGoogleApiClientAuth);
                            session.logoutUser();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap getImage(String image) {
        byte[] imageArray = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.not_supported), Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void findLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            this.latitude = String.valueOf(latitude);
            this.longitude = String.valueOf(longitude);

        } else {

            Toast.makeText(getApplicationContext(), getString(R.string.location_not_access), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        if (isLocationEnabled()) {
            findLocation();
        } else {
            showAlert();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

}
