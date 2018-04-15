package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Saîmon on 11/04/2018.
 */

public class CustomAdapterExpenseRelativeToBudget extends CursorAdapter {
    TextView mExpense;
    TextView mAmount;
    TextView mDate;


    public CustomAdapterExpenseRelativeToBudget(Context context, Cursor c) {
        super(context, c);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_research, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mExpense = (TextView) view.findViewById(R.id.txtName);

        mAmount = (TextView) view.findViewById(R.id.txtAmount);
        mDate = (TextView) view.findViewById(R.id.txtDate);


        String name = cursor.getString(1);
        String amount = cursor.getString(3);
        String date = cursor.getString(4);

        amount = "-$ " + amount;

        mExpense.setText(name);
        mAmount.setText(amount);
        mDate.setText(date);

    }
}
