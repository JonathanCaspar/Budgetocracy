package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by Max on 2018-03-27.
 */

public class CustomAdapterGraph extends CursorAdapter {

    PieChart pieChart;

    public CustomAdapterGraph (Context context, Cursor c){
        super(context,c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_graphics,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        pieChart = (PieChart) view.findViewById(R.id.piechart_1);
        pieChart.setRotationEnabled(true);

        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(75.f);
        pieChart.setTransparentCircleRadius(85f);

        pieChart.setDrawCenterText(true);
        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        // TEXT FROM THE MIDDLE
        SpannableString s = new SpannableString("Budget: 80.0$\nLeft: 25.0$");
        s.setSpan(new RelativeSizeSpan(0.8f), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()),7, 14, 0);
        s.setSpan(new ForegroundColorSpan(Color.rgb(245,58,58)),20, s.length(), 0);
        pieChart.setCenterText(s);

        // ADD VALUES
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(20f, ""));
        yValues.add(new PieEntry(50f, ""));
        PieDataSet dataSet = new PieDataSet(yValues,"Budget");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(0f);

        // ADD COLORS
        ArrayList colors = new ArrayList();
        colors.add(Color.WHITE);
        colors.add(0xFF67DFF2);
        dataSet.setColors(colors);

        PieData data = new PieData((dataSet));
        pieChart.setData(data);
    }

}
