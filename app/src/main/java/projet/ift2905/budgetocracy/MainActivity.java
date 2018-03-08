package projet.ift2905.budgetocracy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Page;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Word;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.apache.commons.codec.binary.Base64;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // INTERFACE
    private BottomNavigationViewEx mBottomBar; // Menu de l'écran principal
    private TextView reponseAPI;

    // DONNEES
    private String photoBase64; // Emplacement de la photo à envoyer
    private Vision vision; // Client API

    final String[] PERMISSIONS = {Manifest.permission.CAMERA,
                                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                  Manifest.permission.INTERNET};
    final int PERMISSION_ALL = 101;
    final int REQUEST_IMAGE_CAPTURE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar (en haut)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getCurrentMonth());

        // Barre de navigation (en bas)
        mBottomBar = findViewById(R.id.menuBar);
        mBottomBar.setActivated(true);
        mBottomBar.enableItemShiftingMode(false);
        mBottomBar.enableAnimation(false);
        mBottomBar.enableShiftingMode(false);
        mBottomBar.setTextVisibility(false);

        // Sert juste à tester l'image récupérée par la caméra
        reponseAPI = findViewById(R.id.reponseAPI);

        // Liens d'écoute sur la barre de navigation
        mBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_scan :
                        // Si Permission accordée:
                        if(hasPermissions(getApplicationContext(), PERMISSIONS)){
                            Intent takePhotoIntent = new Intent(MainActivity.this, CameraActivity.class);
                            // Activité Caméra lancée dans l'attente d'une réponse (image)
                            startActivityForResult(takePhotoIntent ,REQUEST_IMAGE_CAPTURE);
                            return true;
                        } else {
                            // Sinon: les demander
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                            return false;
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

        // Affichage du menu latéral gauche
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Récupère les données attendues d'une activité selon le "requestCode"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK) {
                    photoBase64 = data.getStringExtra("photoBase64");

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            setupAPIClient();
                            runGoogleAPI();
                        }
                    });

                }
        }
    }

    /*********
     *  API  *
     *********/

    // Configure le client qui va interagir avec les serveurs Google
    private void setupAPIClient(){
        Vision.Builder visionBuilder = new Vision.Builder( new NetHttpTransport(), new AndroidJsonFactory(), null);
        String cleAPI = getString(R.string.google_vision_ocr_api_key); // CLE PRIVEE ---> vous DEVEZ vous procurer votre propre clé avec Google
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(cleAPI));
        vision = visionBuilder.build();
    }

    private void runGoogleAPI() {
        // Type d'analyse d'image
        Feature desiredFeature = new Feature();
        desiredFeature.setType("TEXT_DETECTION");

        Image inputImage = new Image();
        inputImage.encodeContent(com.google.api.client.util.Base64.decodeBase64(photoBase64));

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));

        BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));

        // Réponse du serveur
        BatchAnnotateImagesResponse batchResponse = new BatchAnnotateImagesResponse();
        try {
            batchResponse = vision.images().annotate(batchRequest).execute();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Impossible d'accéder à la reconnaissance de textes.", Toast.LENGTH_LONG).show();
            Log.d("API Run :", e.toString());
        }

        final TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
        System.out.println(text.getText());
    }


    /*****************
     *  PERMISSIONS  *
     *****************/
    // Gère la réponse d'un utilisateur à une requête de permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && isAllGranted(grantResults)) {
                    // Permission accordée : accès à l'appareil photo
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                } else {
                    // Permission refusée: impossible de prendre de photo
                    Toast.makeText(getApplicationContext(),"Permission refusée: impossible d'accéder à l'appareil photo.",Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    // Vérification si toutes les permissions demandées ont été acceptés
    public boolean isAllGranted(int[] grantResults){
        for(int i = 0; i < grantResults.length; i++){
            if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }

    //Vérification si les permissions actuelles sont suffisantes
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *  Partie pas importante : gère l'affichage du Drawer (menu latéral)
            **/
    // Fonction utilitaire : générer un fichier .txt de la photo en encodage Base64
    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Affiche le mois courant dans le Toolbar
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
        switch (id){
            case R.id.action_settings:
                return true;

            case R.id.searchMenuItem:
                startActivity(new Intent(MainActivity.this, ResearchActivity.class));
            }
        return super.onOptionsItemSelected(item);
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
