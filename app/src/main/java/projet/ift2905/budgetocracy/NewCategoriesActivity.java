package projet.ift2905.budgetocracy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;

public class NewCategoriesActivity extends AppCompatActivity {

    private ProgressDialog mDialog = null;
    private DBHelper_Budget dbHelper_budget;

    private TextInputLayout categoryName;
    private TextInputLayout categoryBudget;
    private ImageView categoryHint;
    private Button addCategory;
    private TextView currency;

    private final int REQUEST_NEW_CATEGORY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_categorie);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper_budget = new DBHelper_Budget(this);
        categoryName = findViewById(R.id.categoryName);
        categoryBudget = findViewById(R.id.categoryBudget);
        categoryHint = findViewById(R.id.categoryHint);
        addCategory = findViewById(R.id.addCategory);
        currency = findViewById(R.id.currencyNewCategory);

        currency.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("currency","$"));

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInputValidity()) {
                    //Ajout de la dépense à la base de données
                    long id = dbHelper_budget.insertDataName(categoryName.getEditText().getText().toString(),
                            Integer.valueOf(categoryBudget.getEditText().getText().toString()),
                            Integer.valueOf(categoryBudget.getEditText().getText().toString()));

                    Toast.makeText(getApplicationContext(), R.string.successful_category_add, Toast.LENGTH_LONG).show();

                    Intent intent = getIntent();
                    //A checker à la fin de la création
                    if(getIntent().getIntExtra("requestCode", -1) == REQUEST_NEW_CATEGORY){
                        intent.putExtra("newCategoryName", categoryName.getEditText().getText().toString());
                        intent.putExtra("newCategoryID",(int)id);
                    }
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
        categoryName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                categoryName.setErrorEnabled(false);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        categoryBudget.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                categoryBudget.setErrorEnabled(false);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Suggestion de catégories
        categoryHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] listCategory = { getString(R.string.alimentation),
                                          getString(R.string.logement),
                                          getString(R.string.telephone),
                                          getString(R.string.internet),
                                          getString(R.string.loisirs),
                                          getString(R.string.transport)
                };
                //Crée la fenêtre de base
                AlertDialog.Builder builder = new AlertDialog.Builder(NewCategoriesActivity.this);
                builder.setTitle(R.string.categories_sample);
                builder.setItems(listCategory, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        categoryName.getEditText().setText(listCategory[which]);
                    }
                });
                builder.setNegativeButton(R.string.cancel ,null);
                builder.setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    // Vérifie que le formulaire est bien rempli SINON afficher les erreurs appropriées
    public boolean checkInputValidity() {
        boolean allGood = true;
        // Check Name
        if (categoryName.getEditText().getText().length() == 0) {
            categoryName.setError(getString(R.string.empty_name_error));
            categoryName.setErrorEnabled(true);
            allGood = false;
        } else {
            categoryName.setErrorEnabled(false);
        }

        // Check Amount
        String budget = categoryBudget.getEditText().getText().toString();
        if (budget.length() == 0) {
            categoryBudget.setError(getString(R.string.empty_amount_error));
            categoryBudget.setErrorEnabled(true);
            allGood = false;
        }
        else if (Integer.valueOf(budget) <= 0 ){
            categoryBudget.setError(getString(R.string.negative_amount_error));
            categoryBudget.setErrorEnabled(true);
            allGood = false;
        }
        else {
            categoryBudget.setErrorEnabled(false);
        }
        return allGood;
    }
}
