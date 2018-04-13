package projet.ift2905.budgetocracy;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Saîmon on 25/03/2018.
 */

public class CustomAdapterMainBudget extends CursorAdapter{
    TextView mBudget;
    TextView mValueBudget;
    ProgressBar mBarBudget;


    public CustomAdapterMainBudget (Context context, Cursor c){
        super(context,c);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.item_main_budget,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor){
        mBudget = (TextView) view.findViewById(R.id.budgetName);
        mValueBudget = (TextView) view.findViewById(R.id.budgetValue);
        mBarBudget = (ProgressBar) view.findViewById(R.id.budgetBar);


        String name = cursor.getString(1);
        String amount = cursor.getString(3) + " / "+ cursor.getString(2);
        String bar = "TODO";



        mBudget.setText(name);
        int valueBar = Integer.valueOf(cursor.getString(3));
        valueBar = valueBar * 100 / Integer.valueOf(cursor.getString(2));

        mBarBudget.setProgress(valueBar);
        mValueBudget.setText(amount);



    }







}
