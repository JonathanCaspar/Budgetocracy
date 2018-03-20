package projet.ift2905.budgetocracy;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class NewExpensesActivity extends AppCompatActivity {

    private ProgressDialog mDialog = null;
    private Vision vision; // Client API

    private EditText addName;
    private EditText addCategory;
    private EditText addDate;
    private EditText addAmount;
    private Button addButton;

    String[] datasource = {"Alimentation", "Logement", "Téléphone", "Loisirs", "Transport"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expenses);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Données à entrer
        addName = findViewById(R.id.expenseName);
        addCategory = findViewById(R.id.expenseCategory);
        addAmount = findViewById(R.id.expenseAmount);
        addDate = findViewById(R.id.expenseDate);

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewExpensesActivity.this);
                builder.setTitle(R.string.pick_category);
                builder.setItems(datasource, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addCategory.setText(datasource[which]);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        addDate.setText(getCurrentDate());

        // Ajout dû à un scan ?

        if(getIntent().getBooleanExtra("requestDataToAPI",false)){ // Scan effectué
            String[] photoBase64 = {getIntent().getStringExtra("photoBase64")};
            LoadDataFromImage task = new LoadDataFromImage(this);
            task.execute(photoBase64);
        }
    }

    public void showDatePickerDialog(View v){
        DialogFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(),"datePicker");
    }


    public void updateDate(String date){
        addDate.setText(date);
    }

    public static String getCurrentDate(){
        final Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        return (day + "/" + month + "/" + year);
    }


    /*********
     *  API  *
     *********/
    private class LoadDataFromImage extends AsyncTask<String, Integer, String> {
        private Context ctx;

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        public LoadDataFromImage (Context context){
            ctx = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(ctx);
            mDialog.setMessage("Analyse de la photo ...");
            mDialog.setCancelable(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... photoBase64) {

            Vision.Builder visionBuilder = new Vision.Builder( new NetHttpTransport(), new AndroidJsonFactory(), null);
            String cleAPI = "AIzaSyCtMmGlTBQgA28OMFv8ZeCxSkVIh7-9vPk"; // CLE PRIVEE ---> vous DEVEZ vous procurer votre propre clé avec Google
            visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(cleAPI));
            vision = visionBuilder.build();

            // Type d'analyse d'image
            Feature desiredFeature = new Feature();
            desiredFeature.setType("TEXT_DETECTION");

            Image inputImage = new Image();
            inputImage.encodeContent(com.google.api.client.util.Base64.decodeBase64(photoBase64[0]));
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
                Toast.makeText(getApplicationContext(), "Impossible d'accéder à la reconnaissance de textes", Toast.LENGTH_LONG).show();
                Log.d("API Run :", e.toString());
            }

            final TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
            if(text != null){
                String textReceived = text.getText();
                System.out.println(textReceived);
                elapsedTime = ((new Date()).getTime() - startTime)/1000;

                return textReceived;
            }
            else{
                return null;
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            addName.setText(result);
            Toast.makeText(getApplicationContext(), String.format("Analyse effectuée en %.1f s", (float) elapsedTime), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mDialog != null) {
            mDialog.dismiss();
        }
    }
}