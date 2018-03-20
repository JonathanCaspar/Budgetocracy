package projet.ift2905.budgetocracy;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

import static com.github.mikephil.charting.utils.ColorTemplate.*;

public class GraphicActivity extends AppCompatActivity {

    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphics);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        pieChart = (PieChart) findViewById(R.id.piechart_1);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        //pieChart.setHoleRadius(60.f);
        pieChart.setTransparentCircleRadius(61f);

        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(20f, ""));
        yValues.add(new PieEntry(50f, ""));


        PieDataSet dataSet = new PieDataSet(yValues,"Budget");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(0f);

        ArrayList colors = new ArrayList();
        colors.add(Color.WHITE);
        colors.add(0xFF139729);
        dataSet.setColors(colors);

        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        PieData data = new PieData((dataSet));

        pieChart.setData(data);

    }
}
