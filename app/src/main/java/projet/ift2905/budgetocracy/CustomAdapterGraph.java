package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by Max on 2018-03-27.
 */

public class CustomAdapterGraph extends CursorAdapter {

    PieChart pieChart;
    TextView categoryText;

    public CustomAdapterGraph (Context context, Cursor c){
        super(context,c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_graphics,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        categoryText = (TextView) view.findViewById(R.id.textView5);
        categoryText.setText(cursor.getString(1));
        String currency = PreferenceManager.getDefaultSharedPreferences(context).getString("currency","$");

        // Piechart configuration
        pieChart = (PieChart) view.findViewById(R.id.piechart_1);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(75.f);
        pieChart.setTransparentCircleRadius(80f);
        pieChart.setDrawCenterText(true);

        // Disable the legend
        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        // Text from the middle of the piechart graph
        float budget = Float.parseFloat(cursor.getString(2));
        float remaining = Float.parseFloat(cursor.getString(3));
        float usedBudget = budget - remaining;

        SpannableString s = new SpannableString("Budget: "+budget+currency+"\n"+context.getString(R.string.used)+": "+usedBudget+currency+"\n"+context.getString(R.string.left) +": "+remaining+currency);
        s.setSpan(new RelativeSizeSpan(0.8f), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.rgb(16,176,115)),s.length()-1-Float.toString(remaining).length(), s.length(), 0);
        pieChart.setCenterText(s);

        // Add the values to the piechart
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(usedBudget, ""));
        yValues.add(new PieEntry(remaining, ""));
        PieDataSet dataSet = new PieDataSet(yValues,"Budget");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(0f);

        // Piechart bar colors
        ArrayList colors = new ArrayList();
        colors.add(Color.RED);
        colors.add(0xFF10B073);
        dataSet.setColors(colors);

        PieData data = new PieData((dataSet));
        pieChart.setData(data);

    }

}
