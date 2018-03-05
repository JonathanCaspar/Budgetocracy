package projet.ift2905.budgetocracy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationViewEx mBottomBar; // Menu de l'écran principal
    final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getCurrentMonth());
        mBottomBar = (BottomNavigationViewEx) findViewById(R.id.menuBar);
        mBottomBar.setActivated(true);
        mBottomBar.enableItemShiftingMode(false);
        mBottomBar.enableAnimation(false);
        mBottomBar.enableShiftingMode(false);
        mBottomBar.setTextVisibility(false);

        mBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){

                    case R.id.menu_scan :
                        item.setEnabled(true);
                        mBottomBar.setBackgroundResource(R.color.colorPrimary);
                        // Si Permission non accordée:
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

                            // Request the permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                            return false;

                        } else {
                            takePicture();
                            return true;
                        }

                    case R.id.menu_categories:
                        startActivity(new Intent(MainActivity.this, NewCategoriesActivity.class));
                        return true;

                    case R.id.menu_add_expenses:
                        startActivity(new Intent(MainActivity.this, NewExpensesActivity.class));
                        return true;

                    case R.id.menu_graphiques:
                        startActivity(new Intent(MainActivity.this, GraphicActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Gère la réponse d'un utilisateur à une requête de permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission accordée : accès à l'appareil photo
                    takePicture();

                } else {
                    // Permission refusée: impossible de prendre de photo
                    Toast.makeText(getApplicationContext(),"Permission refusée: impossible d'accéder à l'appareil photo.",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
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

    public String getCurrentMonth(){
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        String mois = dateFormat.format(date);

        switch(mois) {
            case "01":
                return "Janvier";
            case "02":
                return "Février";
            case "03":
                return "Mars";
            case "04":
                return "Avril";
            case "05":
                return "Mai";
            case "06":
                return "Juin";
            case "07":
                return "Juillet";
            case "08":
                return "Août";
            case "09":
                return "Septembre";
            case "10":
                return "Octobre";
            case "11":
                return "Novembre";
            case "12":
                return "Décembre";
            default :
                return "Cinglinglin";
        }
    }

}
