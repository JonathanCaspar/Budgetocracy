package projet.ift2905.budgetocracy;

import android.content.Context;
import android.preference.PreferenceManager;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by Max on 2018-04-08.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {
    private DecimalFormat mFormat;
    private Context context;

    public MyAxisValueFormatter(Context context) {
        mFormat = new DecimalFormat("###,###,###,##0.0");
        this.context = context;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String currency = PreferenceManager.getDefaultSharedPreferences(context).getString("currency", "$");
        return mFormat.format(value) + " " + currency;
    }
}
