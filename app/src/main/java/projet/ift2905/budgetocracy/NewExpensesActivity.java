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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
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

    private final int REQUEST_NEW_CATEGORY = 101;
    private ProgressDialog mDialog = null;
    private DBHelper_Budget DB_Budget;
    private DBHelper_Expenses DB_Expenses;
    private Vision vision; // Client API
    private TextInputLayout expenseName;
    private TextInputLayout expenseCategory;
    private TextInputLayout expenseDate;
    private TextInputLayout expenseAmount;
    private EditText expenseAmountValue;
    private SegmentedGroup choixRecurrence;
    private RadioButton uniqueButton;
    private RadioButton recurrenceButton;
    private Button expenseAddButton;
    private TextView currencyNewAmount;
    private int categoryID = -1;
    private SharedPreferences prefs;

    public static String fixUppercase(String str) {
        if (str.length() > 1) {
            String trimStr = str.trim();
            String newStr = trimStr.substring(0, 1).toUpperCase() + trimStr.toLowerCase().substring(1);
            return newStr;
        }
        return "";
    }

    public static String fixShortYear(String year) {
        String newYear = year;
        if (year.length() == 2) {
            newYear = "20" + year;
        }
        return newYear;
    }

    public static String fixDate(String date) {
        String newDate = date;
        if (date.substring(0, 1).equals("0")) {
            newDate = date.substring(1, 2);
        }
        return newDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expenses);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setIcon(R.drawable.ic_note_add_24px);

        DB_Budget = new DBHelper_Budget(this);
        DB_Expenses = new DBHelper_Expenses(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Calendar cal = Calendar.getInstance();
        DateFormat patternDate = new SimpleDateFormat("yyyy-MM-dd");

        // Données à entrer
        expenseName = findViewById(R.id.expenseName);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseAmountValue = findViewById(R.id.expenseAmountValue);
        expenseDate = findViewById(R.id.expenseDate);
        choixRecurrence = findViewById(R.id.choixRecurrence);
        uniqueButton = findViewById(R.id.choix_unique_button);
        recurrenceButton = findViewById(R.id.choix_recurrent_button);
        expenseAddButton = findViewById(R.id.addExpense);
        currencyNewAmount = findViewById(R.id.currencyNewAmount);

        // Changement de couleur choixRecurrence
        choixRecurrence.setTintColor(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorIcons));

        // Affichage de la devise pour le montant
        currencyNewAmount.setText(prefs.getString("currency", "$"));

        Intent intent = getIntent();
        // Ajout de données via un scan ?
        if (intent.getBooleanExtra("requestDataToAPI", false)) { // Scan effectué
            String[] photoBase64 = {intent.getStringExtra("photoBase64")};
            LoadDataFromImage task = new LoadDataFromImage(this);
            task.execute(photoBase64);
        }

        // Modification de dépense ?
        if (intent.getBooleanExtra("requestModifyData", false)) {
            ab.setTitle(" " + getString(R.string.modifierDepense));
            String strId = intent.getStringExtra("idExpenseToModify");
            Cursor expense = DB_Expenses.getExpense(strId);

            String tmpName = expense.getString(1);
            categoryID = expense.getInt(2);
            String tmpCategorie = DB_Budget.getStringBudgetWithID(expense.getString(2));
            String tmpAmount = expense.getString(3);
            intent.putExtra("oldExpenseValue", expense.getFloat(3));

            String tmpDate = expense.getString(4);

            expenseName.getEditText().setText(tmpName);
            expenseDate.getEditText().setText(tmpDate);
            expenseAmountValue.setText(tmpAmount);
            expenseCategory.getEditText().setText(tmpCategorie);
        } else {
            expenseDate.getEditText().setText(patternDate.format(cal.getTime()));
        }

        // Catégorie déjà connue ?
        if (intent.hasExtra("categoryID")) {
            categoryID = Integer.valueOf(intent.getStringExtra("categoryID"));
            System.out.println("has extra Category ID = " + categoryID);
            String tmpCategorie = DB_Budget.getStringBudgetWithID(String.valueOf(categoryID));
            System.out.println("has extra tmpCategory = " + tmpCategorie);
            expenseCategory.getEditText().setText(tmpCategorie);
        }

        // Choix de catégorie
        expenseCategory.getEditText().setOnClickListener(new View.OnClickListener() {
            private AlertDialog alertDialog = null;

            @Override
            public void onClick(View v) {
                //Récupère la liste des budgets
                Cursor data = DB_Budget.getAllData();

                //Crée la fenêtre de base
                AlertDialog.Builder builder = new AlertDialog.Builder(NewExpensesActivity.this);
                builder.setTitle(R.string.pick_category);

                if (data.getCount() > 0) {
                    final HashMap<Integer, String> categoriesWithID = DB_Budget.getBudgetList();
                    final String[] categories = new String[categoriesWithID.size()];
                    final int[] IDs = new int[categoriesWithID.size()];
                    int i = 0;


                    Iterator it = categoriesWithID.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry budget = (HashMap.Entry) it.next();
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
                } else {
                    builder.setMessage(R.string.empty_category_db);
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(NewExpensesActivity.this, NewCategoriesActivity.class);
                            intent.putExtra("requestCode", REQUEST_NEW_CATEGORY);
                            startActivityForResult(intent, REQUEST_NEW_CATEGORY);
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

        uniqueButton.setChecked(true);
        recurrenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uniqueButton.setChecked(true);
                Snackbar.make(findViewById(R.id.expenseCoordinatorLayout), "Dépense récurrente non implémentée", Snackbar.LENGTH_SHORT).show();
            }
        });

        expenseAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInputValidity()) {

                    String repeat = "0"; //dépense unique par défaut
                    if (recurrenceButton.isChecked()) {
                        repeat = "1";
                    }

                    String[] data = {expenseName.getEditText().getText().toString(),
                            String.valueOf(categoryID),
                            expenseAmountValue.getText().toString(),
                            expenseDate.getEditText().getText().toString(),
                            repeat};

                    Intent intent = getIntent();
                    intent.putExtra("dataToSave", data);

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
        expenseAmountValue.addTextChangedListener(new TextWatcher() {
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
        switch (requestCode) {
            case REQUEST_NEW_CATEGORY:
                if (resultCode == RESULT_OK) {
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
        if (expenseAmountValue.getText().length() == 0) {
            expenseAmount.setError(getString(R.string.empty_amount_error));
            expenseAmount.setErrorEnabled(true);
            allGood = false;
        } else {
            expenseAmount.setErrorEnabled(false);
        }
        return allGood;
    }

    public void updateDate(String date) {
        expenseDate.getEditText().setText(date);
    }

    // Partie Menu (Editer, supprimer)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra("requestModifyData", false)) {
            getMenuInflater().inflate(R.menu.expenses_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteExpenseOptions:
                String strId = getIntent().getStringExtra("idExpenseToModify");
                Cursor expenseData = DB_Expenses.getExpense(strId); // Toutes les informations de la dépense sélectionné

                if (expenseData != null) {
                    String expenseDate = expenseData.getString(4).split("-")[1];
                    if (MainActivity.isSameMonthAsCurrent(expenseDate)) {
                        // Mets à jour le reste du budget associé à la dépense si cette dernière et enregistré pour le mois courant
                        DB_Budget.increaseRemainingAmount(expenseData.getInt(2), expenseData.getFloat(3));
                    }
                    DB_Expenses.deleteData(strId);
                    Toast.makeText(getApplicationContext(), R.string.successful_expense_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "datePicker");
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

    /*********
     *  API  *
     *********/
    private class LoadDataFromImage extends AsyncTask<String, Integer, String> {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        private Context ctx;

        public LoadDataFromImage(Context context) {
            ctx = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(ctx);
            mDialog.setMessage(getString(R.string.photo_analysis));
            mDialog.setCancelable(false);
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.photo_analysis_cancelled, Toast.LENGTH_LONG).show();
                    cancel(true);
                }
            });
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... photoBase64) {
            while(!isCancelled()) {
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
                } else {
                    result += " ";
                }
                result += "-#-"; //Séparateur

                // Affichage du texte
                if (text != null) {
                    String textReceived = text.getText();
                    System.out.println(textReceived);
                    elapsedTime = ((new Date()).getTime() - startTime) / 1000;
                    result += textReceived;
                } else {
                    result += " ";
                }
                String finalResult = fixUppercase(result);
                System.out.println("resultFinal = " + finalResult);
                return finalResult;
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            mDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            String[] reponse = result.split("-#-");
            System.out.println("RESULT API -------) " + result);
            String logo = "";
            String text = "";

            if (!result.equals("-#-")) {
                //Tentative de lecture du logo
                try {
                    logo = reponse[0];
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                //Tentative de lecture du texte
                try {
                    text = reponse[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            String montant = "";

            // Détecte un pattern (total digit.digit)
            Pattern pattern = Pattern.compile("\\b(TOTAL|Total|total)(\\s)*+(\\d)+.(\\d)+");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    System.out.println("group " + i + ": " + matcher.group(i));
                }
                // Extrait le montant
                String totalText = matcher.group(0);
                Pattern patternMontant = Pattern.compile("(\\s)*+(\\d)+.(\\d)+");
                Matcher matcherMontant = patternMontant.matcher(totalText);

                if (matcherMontant.find()) {
                    montant = (matcherMontant.group(0)).trim();
                }
            }

            // Détecte la date
            Pattern patternDate = Pattern.compile("\\d\\d(\\s)*/(\\s)*\\d\\d(\\s)*/(\\s)*(\\d{4}|\\d{2})");
            Matcher matcherDate = patternDate.matcher(text);
            if (matcherDate.find()) {
                System.out.println("dateRegex = " + matcherDate.group(0));
                String[] date = matcherDate.group(0).trim().split("/");
                String newDate = fixShortYear(date[2]) + "-" + fixDate(date[1]) + "-" + date[0];
                expenseDate.getEditText().setText(newDate);
            }

            expenseName.getEditText().setText(logo);
            expenseAmountValue.setText(montant);
        }

        @Override
        protected void onCancelled() {
            cancel(true);
        }
    }
}