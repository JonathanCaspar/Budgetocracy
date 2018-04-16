package projet.ift2905.budgetocracy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Saîmon on 11/04/2018.
 */

public class ExpensesRelativeToBudget extends AppCompatActivity {
    private DBHelper_Budget dbHelper_budget;
    private DBHelper_Expenses dbHelper_expenses;
    private ListView mListViewExpenseB;
    private FloatingActionButton addExpenseButton;
    private CustomAdapterExpenseRelativeToBudget cursorExpenseRelatToB;
    private Cursor cursorB;
    private String categoryID;
    private RelativeLayout rLay;

    final int REQUEST_EXPENSE_DATA = 103;
    final int REQUEST_MODIFICATION_EXPENSE_DATA = 105;
    final int REQUEST_MODIFICATION_BUDGET_DATA = 106;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHelper_budget = new DBHelper_Budget(this);
        dbHelper_expenses = new DBHelper_Expenses(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_relative_to_budget);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Accès aux views
        mListViewExpenseB = findViewById(R.id.lstExpensesRelativeToBudget);
        addExpenseButton = findViewById(R.id.floatingAddExpense);
        rLay = findViewById(R.id.popupNoExpense);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        categoryID = getIntent().getStringExtra("idBudget");
        ab.setTitle(dbHelper_budget.getStringBudgetWithID(categoryID));

        loadCursor(categoryID);
        checkIfEmpty();

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpensesRelativeToBudget.this, NewExpensesActivity.class);
                intent.putExtra("categoryID", categoryID);
                startActivityForResult(intent, REQUEST_EXPENSE_DATA);
            }
        });

        mListViewExpenseB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String expenseID = String.valueOf(id);
                String[] list = {getResources().getString(R.string.modify), getResources().getString(R.string.delete)};

                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(true);
                builder.setTitle(((Cursor)parent.getAdapter().getItem(position)).getString(1));
                builder.setIcon(R.drawable.ic_edit_black_24dp);
                builder.setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0: // Modification
                                Intent modifyExpense = new Intent(ExpensesRelativeToBudget.this, NewExpensesActivity.class);
                                modifyExpense.putExtra("requestModifyData", true);
                                modifyExpense.putExtra("idExpenseToModify", expenseID);
                                startActivityForResult(modifyExpense, REQUEST_MODIFICATION_EXPENSE_DATA);
                                break;

                            case 1: // Suppression
                                Cursor expenseData = dbHelper_expenses.getExpense(expenseID); // Toutes les informations de la dépense sélectionné

                                if (expenseData != null) {
                                    String expenseDate = expenseData.getString(4).split("-")[1];
                                    if (MainActivity.isSameMonthAsCurrent(expenseDate)) {
                                        // Mets à jour le reste du budget associé à la dépense si cette dernière et enregistré pour le mois courant
                                        dbHelper_budget.increaseRemainingAmount(expenseData.getInt(2), expenseData.getFloat(3));
                                    }
                                    dbHelper_expenses.deleteData(expenseID);
                                    loadCursor(categoryID);
                                    checkIfEmpty();
                                    Toast.makeText(getApplicationContext(), R.string.successful_expense_deleted, Toast.LENGTH_SHORT).show();

                                }
                                break;

                            default:
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
    }

    // Refresh la view
    public void loadCursor(String ID) {
        cursorB = dbHelper_expenses.getExpensesAssociateToBudget(ID);
        cursorExpenseRelatToB = new CustomAdapterExpenseRelativeToBudget(this, cursorB);
        mListViewExpenseB.setAdapter((ListAdapter) cursorExpenseRelatToB);
    }

    public void checkIfEmpty() {
        // Si notre budget ne contient pas de dépense
        if (mListViewExpenseB.getCount() == 0) {
            rLay.setVisibility(View.VISIBLE);
        } else {
            rLay.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.expenses_of_budget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editOptions:
                // On appelle l'activité pour modifier la dépense (qui est en soit l'activité de création de dépense, auxquelle on fourni un extra particulier)
                Intent modifyExpense = new Intent(ExpensesRelativeToBudget.this, NewCategoriesActivity.class);
                modifyExpense.putExtra("requestModifyData", true);
                modifyExpense.putExtra("idBudgetToModify", categoryID);
                startActivityForResult(modifyExpense, REQUEST_MODIFICATION_BUDGET_DATA);
                break;

            case R.id.deleteOptions:
                Cursor expensesOfBudget = dbHelper_expenses.getExpensesAssociateToBudget(categoryID); // Toutes les informations du budget sélectionné
                if (expensesOfBudget != null) {
                    if (expensesOfBudget.getCount() > 0) {
                        // Afficher un message avisant l'utilisateur de la suppression des dépenses associées
                        final AlertDialog.Builder message = new AlertDialog.Builder(ExpensesRelativeToBudget.this);
                        message.setCancelable(true);
                        message.setMessage(R.string.existing_expenses_warning);
                        message.setIcon(R.drawable.ic_delete_black_24dp);
                        message.setTitle(R.string.existing_expenses_warning_title);
                        message.setNegativeButton(R.string.cancel, null);
                        message.setPositiveButton(R.string.delete_anyway, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper_budget.deleteData(categoryID);
                                dbHelper_expenses.deleteExpensesOfBudget(categoryID);
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.successful_budget_deleted, Snackbar.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        message.show();
                    }
                } else {
                    // Afficher un message avisant l'utilisateur de la suppression des dépenses associées
                    final AlertDialog.Builder message = new AlertDialog.Builder(ExpensesRelativeToBudget.this);
                    message.setCancelable(true);
                    message.setMessage(R.string.delete_budget_msg);
                    message.setIcon(R.drawable.ic_delete_black_24dp);
                    message.setTitle(R.string.delete_budget);
                    message.setNegativeButton(R.string.cancel, null);
                    message.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper_budget.deleteData(categoryID);
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.successful_budget_deleted, Snackbar.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    message.show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MODIFICATION_EXPENSE_DATA:
                if (resultCode == RESULT_OK && data != null) {
                    String[] dataToModify = data.getStringArrayExtra("dataToSave");
                    String idExpense = data.getStringExtra("idExpenseToModify");
                    Float oldExpenseValue = data.getFloatExtra("oldExpenseValue", 0);

                    String expenseMonth = dataToModify[3].split("-")[1];
                    Integer categoryID = Integer.valueOf(dataToModify[1]);

                    if (MainActivity.isSameMonthAsCurrent(expenseMonth)) {
                        dbHelper_budget.increaseRemainingAmount(categoryID, oldExpenseValue);
                        dbHelper_budget.substractRemainingAmount(categoryID, Float.valueOf(dataToModify[2]));
                    }
                    dbHelper_expenses.updateData(idExpense, dataToModify[0], categoryID, Float.valueOf(dataToModify[2]), dataToModify[3]);
                    Toast.makeText(getApplicationContext(), R.string.successful_expense_modification, Toast.LENGTH_LONG).show();

                    loadCursor(String.valueOf(categoryID));
                    checkIfEmpty();
                }
                break;

            case REQUEST_MODIFICATION_BUDGET_DATA:
                if (resultCode == RESULT_OK && data != null) {
                    String[] dataToModify = data.getStringArrayExtra("dataToModify");
                    String idExpense = data.getStringExtra("idBudgetToModify");
                    Float oldBudgetValue = Float.valueOf(data.getStringExtra("oldBudgetValue"));
                    Float newBudgetValue = Float.valueOf(dataToModify[1]);
                    Float remaining = dbHelper_budget.getRemaining(idExpense);

                    Float newRemaining = newBudgetValue - (oldBudgetValue - remaining);

                    // Calcul du nouveau remaining (selon la différence entre l'ancienne valeur et la nouvelle)
                    dbHelper_budget.updateData(idExpense, dataToModify[0], Float.valueOf(dataToModify[1]), newRemaining);
                    getSupportActionBar().setTitle(dataToModify[0]);
                    loadCursor(categoryID);
                    Snackbar.make(findViewById(R.id.expenseRelativeToBudgetCoordinator), R.string.successful_edited_budget, Snackbar.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_EXPENSE_DATA:
                if (resultCode == RESULT_OK && data != null) {
                    String[] dataToAdd = data.getStringArrayExtra("dataToSave");

                    Integer budgetID = Integer.valueOf(dataToAdd[1]);
                    String expenseMonth = dataToAdd[3].split("-")[1];

                    if (MainActivity.isSameMonthAsCurrent(expenseMonth)) {
                        dbHelper_budget.substractRemainingAmount(budgetID, Float.valueOf(dataToAdd[2]));
                    }
                    dbHelper_expenses.insertDataName(dataToAdd[0], budgetID, Float.valueOf(dataToAdd[2]), dataToAdd[3]);

                    if (dataToAdd[1].equals(categoryID)) {// Si la dépense ajoutéé est liée au budget actuellement affiché : rafraichir
                        loadCursor(categoryID);
                    }
                    checkIfEmpty();
                    Toast.makeText(getApplicationContext(), R.string.successful_expense_add, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
