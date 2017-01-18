package com.nrupeshpatel.cleancity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.format.Time;
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
import android.widget.Toast;

import com.nrupeshpatel.cleancity.fragments.HomeFragment;
import com.nrupeshpatel.cleancity.helper.PermissionUtils;
import com.nrupeshpatel.cleancity.helper.PrefManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomeFragment.OnFragmentInteractionListener {

    private PrefManager prefManager;
    private String realPath = null;
    public static final int CAMERA_PERMISSIONS_REQUEST = 1;
    public static final int CAMERA_IMAGE_REQUEST = 2;
    private TabLayout tabLayout;
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private int[] tabIcons = {
            R.drawable.ic_home_tab,
            R.drawable.ic_pending_tab,
            R.drawable.ic_solved_tab,
            R.drawable.ic_star_tab
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Home");

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new HomeFragment(), "Pending");
        adapter.addFragment(new HomeFragment(), "Solved");
        adapter.addFragment(new HomeFragment(), "Starred");
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setTitle(mFragmentTitleList.get(position));
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
                Manifest.permission.CAMERA)) {
            Uri mHighQualityImageUri = generateTimeStampPhotoFileUri();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mHighQualityImageUri);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
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
            outputDir = new File(photoDir, "MedicineWatch");
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
            //uploadImage(Uri.fromFile(getCameraFile()));
            Intent i = new Intent(this, CaptureImageActivity.class);
            i.putExtra("realPath", realPath);
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
                                    .setIcon(R.drawable.ic_menu_actionbar)
                                    .setIconDrawableColourFilter(getResources().getColor(R.color.colorPrimary));
                            final Toolbar tb = (Toolbar) MainActivity.this.findViewById(R.id.toolbar);
                            tapTargetPromptBuilder.setTarget(tb.getChildAt(1));

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
                                                .setTarget(R.id.action_settings)
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
