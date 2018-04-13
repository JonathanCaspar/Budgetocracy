package projet.ift2905.budgetocracy;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.hoang8f.android.segmented.SegmentedGroup;

public class NewExpensesActivity extends AppCompatActivity {

    private ProgressDialog mDialog = null;
    private DBHelper_Budget dbHelper_budget;
    private DBHelper_Expenses DB_Expenses;
    private Vision vision; // Client API

    private TextInputLayout expenseName;
    private TextInputLayout expenseCategory;
    private TextInputLayout expenseDate;
    private TextInputLayout expenseAmount;
    private SegmentedGroup choixRecurrence;
    private RadioButton uniqueButton;
    private RadioButton recurrenceButton;
    private Button expenseAddButton;

    private int categoryID = -1;
    private final int REQUEST_NEW_CATEGORY = 101;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expenses);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        dbHelper_budget = new DBHelper_Budget(this);
        DB_Expenses = new DBHelper_Expenses(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Données à entrer
        expenseName = findViewById(R.id.expenseName);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseDate = findViewById(R.id.expenseDate);
        choixRecurrence = findViewById(R.id.choixRecurrence);
        uniqueButton = findViewById(R.id.choix_unique_button);
        recurrenceButton = findViewById(R.id.choix_recurrent_button);
        expenseAddButton = findViewById(R.id.addExpense);

        // Changement de couleur choixRecurrence
        choixRecurrence.setTintColor(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorIcons));

        // Ajout de données via un scan ?
        if (getIntent().getBooleanExtra("requestDataToAPI", false)) { // Scan effectué
            String[] photoBase64 = {getIntent().getStringExtra("photoBase64")};
            LoadDataFromImage task = new LoadDataFromImage(this);
            task.execute(photoBase64);
        }

        // Modification de dépense ?
        if (getIntent().getBooleanExtra("requestModifyData",false)){
            ab.setTitle(R.string.modifierDepense);
            String strId = getIntent().getStringExtra("idExpenseToModify");

            String tmpName = DB_Expenses.getNameExpenseWithID(strId);
            String tmpCategorie = DB_Expenses.getCategorieExpenseWithID(strId);
            categoryID = Integer.valueOf(tmpCategorie);
            tmpCategorie = dbHelper_budget.getStringBudgetWithID(tmpCategorie);
            String tmpAmount = DB_Expenses.getAmountExpenseWithID(strId);
            String tmpDate = DB_Expenses.getDateExpenseWithID(strId);

            expenseName.getEditText().setText(tmpName);
            expenseDate.getEditText().setText(tmpDate);
            expenseAmount.getEditText().setText(tmpAmount);
            expenseCategory.getEditText().setText(tmpCategorie);
        }
        else{
            expenseDate.getEditText().setText(MainActivity.getCurrentDate(getApplicationContext()));
        }

        // Choix de catégorie
        expenseCategory.getEditText().setOnClickListener(new View.OnClickListener() {
            private AlertDialog alertDialog = null;

            @Override
            public void onClick(View v) {
                //Récupère la liste des budgets
                Cursor data = dbHelper_budget.getAllData();

                //Crée la fenêtre de base
                AlertDialog.Builder builder = new AlertDialog.Builder(NewExpensesActivity.this);
                builder.setTitle(R.string.pick_category);

                if(data.getCount() > 0) {
                    final HashMap<Integer,String> categoriesWithID = dbHelper_budget.getBudgetList();
                    final String[] categories = new String[categoriesWithID.size()];
                    final int[] IDs = new int[categoriesWithID.size()];
                    int i = 0;


                    Iterator it = categoriesWithID.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry budget = (HashMap.Entry)it.next();
                        IDs[i] = (Integer) budget.getKey();
                        categories[i] = (String) budget.getValue();
                        it.remove(); // avoids a ConcurrentModificationException
                        i++;
                    }

                    builder.setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            expenseCategory.getEditText().setText(categories[which]);
                            categoryID = IDs[which];
                        }
                    });
                }
                else{
                    builder.setMessage(R.string.empty_category_db);
                    builder.setNegativeButton(R.string.cancel,null);
                    builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(NewExpensesActivity.this, NewCategoriesActivity.class);
                            intent.putExtra("requestCode", REQUEST_NEW_CATEGORY);
                            startActivityForResult(intent,REQUEST_NEW_CATEGORY);
                        }
                    });
                }
                builder.setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        // Montant
        expenseAmount.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                s.append(" " + prefs.getString("currency", "$"));
            }
        });

        // Choix de la fréquence de période
        recurrenceButton.setOnClickListener(new View.OnClickListener() {

            private AlertDialog alertDialog = null;
            private int choix = 0;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewExpensesActivity.this);

                builder.setTitle(R.string.frequency);
                builder.setIcon(R.drawable.ic_timer_24dp);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choix == 0) {
                            Toast.makeText(getApplicationContext(), "Veuillez d'abord choisir une option.", Toast.LENGTH_LONG).show();
                        } else {
                            switch (choix) {
                                case 1:
                                    recurrenceButton.setText("Chaque semaine");
                                    choix = 0;
                                    break;
                                case 2:
                                    recurrenceButton.setText("Chaque mois");
                                    choix = 0;
                                    break;
                                case 3:
                                    recurrenceButton.setText("Chaque année");
                                    choix = 0;
                                    break;
                            }
                            alertDialog.dismiss();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                        uniqueButton.setChecked(true);
                    }
                });
                View popupLayout = getLayoutInflater().inflate(R.layout.popup_choix_recurrence, null);
                builder.setView(popupLayout);

                // Elements du popup
                final SegmentedGroup popup = (SegmentedGroup) popupLayout.findViewById(R.id.popupRecurrenceChoix);
                RadioButton freqBtn1 = popup.findViewById(R.id.every_week);
                RadioButton freqBtn2 = popup.findViewById(R.id.every_month);
                RadioButton freqBtn3 = popup.findViewById(R.id.every_year);

                // Liens d'écoute sur boutons de récurrence
                freqBtn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choix = 1;
                    }
                });

                freqBtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choix = 2;
                    }
                });

                freqBtn3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choix = 3;
                    }
                });

                builder.setCancelable(true);
                alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        uniqueButton.setChecked(true);
        uniqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recurrenceButton.setText(R.string.choix_recurrent_button);
            }
        });

        expenseAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Si on est dans le cas d'une modification de dépense
                if (checkInputValidity() && getIntent().getBooleanExtra("requestModifyData",false)){
                    //Modification de la dépense dans la base de données
                    String[] dataToModify = {expenseName.getEditText().getText().toString(),
                            String.valueOf(categoryID),
                            expenseAmount.getEditText().getText().toString(),
                            expenseDate.getEditText().getText().toString()};

                    Toast.makeText(getApplicationContext(), String.valueOf(categoryID), Toast.LENGTH_LONG).show();

                    String strId = getIntent().getStringExtra("idExpenseToModify");

                    Intent intent = getIntent();
                    intent.putExtra("dataToModify", dataToModify);
                    intent.putExtra("IdExpense",strId);
                    setResult(RESULT_OK, intent);
                    finish();

                }


                else if (checkInputValidity()) {
                    //Ajout de la dépense à la base de données
                    String[] dataToSave = {expenseName.getEditText().getText().toString(),
                            String.valueOf(categoryID),
                            expenseAmount.getEditText().getText().toString(),
                            expenseDate.getEditText().getText().toString()};

                    Intent intent = getIntent();
                    intent.putExtra("dataToSave", dataToSave);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    try {
                        // Cache le clavier
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                    }
                }
            }
        });

        // Retire l'affichage d'erreur lors d'une saisie
        expenseName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseName.setErrorEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        expenseCategory.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseCategory.setErrorEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        expenseAmount.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseAmount.setErrorEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_NEW_CATEGORY:
                if(resultCode == RESULT_OK){
                    String name = data.getStringExtra("newCategoryName");
                    categoryID = data.getIntExtra("newCategoryID", -1);
                    expenseCategory.getEditText().setText(name);
                }
                break;
        }
    }

    // Vérifie que le formulaire est bien rempli SINON afficher les erreurs appropriées
    public boolean checkInputValidity() {
        boolean allGood = true;
        // Check Name
        if (expenseName.getEditText().getText().length() == 0) {
            expenseName.setError(getString(R.string.empty_name_error));
            expenseName.setErrorEnabled(true);
            allGood = false;
        } else {
            expenseName.setErrorEnabled(false);
        }

        // Check Category
        if (expenseCategory.getEditText().getText().length() == 0) {
            expenseCategory.setError(getString(R.string.empty_category_error));
            expenseCategory.setErrorEnabled(true);
            allGood = false;
        } else {
            expenseCategory.setErrorEnabled(false);
        }

        // Check Amount
        if (expenseAmount.getEditText().getText().length() == 0) {
            expenseAmount.setError(getString(R.string.empty_amount_error));
            expenseAmount.setErrorEnabled(true);
            allGood = false;
        } else {
            expenseAmount.setErrorEnabled(false);
        }
        return allGood;
    }

    public void updateDate(String date){
        expenseDate.getEditText().setText(date);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static String fixUppercase(String str){
        if (str.length() > 1){
            String trimStr = str.trim();
            String newStr = trimStr.substring(0, 1).toUpperCase() + trimStr.toLowerCase().substring(1);
            return newStr;
        }
        return "";
    }

    public static String fixShortYear(String year){
        String newYear = year;
        if (year.length() == 2){
            newYear = "20" + year;
        }
        return newYear;
    }

    public static String fixDate(String date){
        String newDate = date;
        if(date.substring(0,1).equals("0")){
            newDate = date.substring(1,2);
        }
        return newDate;
    }

    /*********
     *  API  *
     *********/
    private class LoadDataFromImage extends AsyncTask<String, Integer, String> {
        private Context ctx;

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        public LoadDataFromImage(Context context) {
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
            // A RECUPERER : logoAnnotations/description & textannotations/description

            Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(), new AndroidJsonFactory(), null);
            String cleAPI = "AIzaSyCtMmGlTBQgA28OMFv8ZeCxSkVIh7-9vPk"; // CLE PRIVEE ---> vous DEVEZ vous procurer votre propre clé avec Google
            visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(cleAPI));
            vision = visionBuilder.build();

            // Type d'analyse d'image
            Feature featureLogo = new Feature();
            Feature featureText = new Feature();
            featureLogo.setType("LOGO_DETECTION");
            featureText.setType("TEXT_DETECTION");

            Image inputImage = new Image();
            inputImage.encodeContent(com.google.api.client.util.Base64.decodeBase64(photoBase64[0]));
            AnnotateImageRequest request = new AnnotateImageRequest();
            request.setImage(inputImage);
            request.setFeatures(Arrays.asList(featureLogo, featureText));
            try {
                System.out.println("Requete: " + request.toPrettyString());
            } catch (IOException e) {
                e.printStackTrace();
            }

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
            final List<EntityAnnotation> logo = batchResponse.getResponses().get(0).getLogoAnnotations();

            // Affichage de la réponse logo
            String result = "";

            if (logo != null) {
                result += logo.get(0).getDescription();
            }
            else{
                result += " ";
            }
            result += "-#-"; //Séparateur

            // Affichage du texte
            if (text != null) {
                String textReceived = text.getText();
                System.out.println(textReceived);
                elapsedTime = ((new Date()).getTime() - startTime) / 1000;
                result += textReceived;
            }
            else{
                result += " ";
            }
            String finalResult = fixUppercase(result);
            System.out.println("resultFinal = " + finalResult);
            return finalResult;
        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            String[] reponse = result.split("-#-");
            String logo = reponse[0];
            String text = reponse[1];
            String montant = "";

            // Détecte un pattern (total digit.digit)
            Pattern pattern = Pattern.compile("\\b(TOTAL|Total|total)(\\s)*+(\\d)+.(\\d)+");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                for(int i = 0; i < matcher.groupCount(); i++){
                    System.out.println("group "+i +": "+matcher.group(i));
                }
                // Extrait le montant
                String totalText = matcher.group(0);
                Pattern patternMontant = Pattern.compile("(\\s)*+(\\d)+.(\\d)+");
                Matcher matcherMontant = patternMontant.matcher(totalText);

                if (matcherMontant.find()){
                    montant = (matcherMontant.group(0)).trim();
                }
            }

            // Détecte la date
            Pattern patternDate = Pattern.compile("\\d\\d(\\s)*/(\\s)*\\d\\d(\\s)*/(\\s)*(\\d{4}|\\d{2})");
            Matcher matcherDate = patternDate.matcher(text);
            if (matcherDate.find()) {
                System.out.println("dateRegex = " + matcherDate.group(0));
                String[] date = matcherDate.group(0).trim().split("/");
                String newDate = fixDate(date[1]) +  MainActivity.getMonthFromNb(date[0], getApplicationContext()) + fixShortYear(date[2]);
                expenseDate.getEditText().setText(newDate);
            }

            expenseName.getEditText().setText(logo);
            expenseAmount.getEditText().setText(montant);

            Toast.makeText(getApplicationContext(), String.format("Analyse effectuée en %.1f s", (float) elapsedTime), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}