package projet.ift2905.budgetocracy;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Saîmon on 11/04/2018.
 */

public class ExpensesRelativeToBudget extends AppCompatActivity {
    private DBHelper_Budget dbHelper_budget;
    private DBHelper_Expenses dbHelper_expenses;
    ListView mListViewExpenseB;
    private CustomAdapterExpenseRelativeToBudget cursorExpenseRelatToB;
    Cursor cursorB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHelper_budget = new DBHelper_Budget(this);
        dbHelper_expenses = new DBHelper_Expenses(this);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_relative_to_budget);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        String strId = getIntent().getStringExtra("idBudget");
        ab.setTitle(dbHelper_budget.getStringBudgetWithID(strId));

        cursorB = dbHelper_expenses.getExpensesAssociateToBudget(strId);

        mListViewExpenseB = findViewById(R.id.lstExpensesRelativeToBudget);
        cursorExpenseRelatToB = new CustomAdapterExpenseRelativeToBudget(this,cursorB);
        mListViewExpenseB.setAdapter((ListAdapter) cursorExpenseRelatToB);






    }


}
