package projet.ift2905.budgetocracy;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class CustomAdapter extends CursorAdapter{
    TextView mExpense;
    TextView mCategorie;
    TextView mAmount;
    TextView mDate;

    SharedPreferences prefs;
    DBHelper_Budget db_budget; //accès aux méthodes de la DB

    public CustomAdapter (Context context, Cursor c){
        super(context,c);
        db_budget = new DBHelper_Budget(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.item_research,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        mExpense = (TextView) view.findViewById(R.id.txtName);
        mCategorie = (TextView) view.findViewById(R.id.txtCategorie);
        mAmount = (TextView) view.findViewById(R.id.txtAmount);
        mDate = (TextView) view.findViewById(R.id.txtDate);

        String title = cursor.getString(1);
        String categorie = db_budget.getStringBudgetWithID(cursor.getString(2));
        String amount = "- " + cursor.getString(3) + " " + prefs.getString("currency", "$");
        String date = cursor.getString(4);

        mExpense.setText(title);
        mCategorie.setText(categorie);
        mAmount.setText(amount);
        mDate.setText(date);
    }
}