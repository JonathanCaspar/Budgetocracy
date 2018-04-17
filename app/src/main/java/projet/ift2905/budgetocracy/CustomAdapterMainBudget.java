package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Saîmon on 25/03/2018.
 */

public class CustomAdapterMainBudget extends CursorAdapter {
    TextView mBudget;
    TextView mValueBudget;
    ProgressBar mBarBudget;
    ImageView arrow;


    public CustomAdapterMainBudget(Context context, Cursor c) {
        super(context, c);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_main_budget, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mBudget = (TextView) view.findViewById(R.id.budgetName);
        mValueBudget = (TextView) view.findViewById(R.id.budgetValue);
        mBarBudget = (ProgressBar) view.findViewById(R.id.budgetBar);
        arrow = (ImageView) view.findViewById(R.id.arrowBudget);

        String name = cursor.getString(1);
        String amount = cursor.getString(3) + " / " + cursor.getString(2);

        mBudget.setText(name);
        float valueBar = Float.valueOf(cursor.getString(3));
        float valuePercent = valueBar * 100 / Float.valueOf(cursor.getString(2));

        mBarBudget.setProgress((int) valuePercent);

        if (valueBar >= 0) {
            mBarBudget.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_bar_positive));
            mBarBudget.setBackgroundColor(context.getResources().getColor(R.color.progress_background));
        } else {
            mBarBudget.setProgressDrawable(null);
            mBarBudget.setBackgroundColor(context.getResources().getColor(R.color.progress_negative));
        }
        mValueBudget.setText(amount);
    }
}
