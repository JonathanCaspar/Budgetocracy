package projet.ift2905.budgetocracy;


import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import info.hoang8f.android.segmented.SegmentedGroup;

public class GraphicActivity extends AppCompatActivity {

    Cursor cursor;

    PieChart pieChart;
    RadarChart radarChart;
    BarChart barChart;
    TextView barChart_Textview;

    RadioButton graph_button_1;
    RadioButton graph_button_2;
    RadioButton graph_button_3;

    // Radar Chart design parameters
    int r_BackGround = Color.rgb(60, 65, 82);
    int r_Web = Color.WHITE;
    int r_innerWeb = Color.LTGRAY;
    float r_TextSize = 12f;
    int r_TextColor = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphics);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_1));
        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.drawable.ic_pie_chart_outlined_white_24dp);
        ab.setTitle(" "+getString(R.string.graphics));
        ab.setDisplayHomeAsUpEnabled(true);

        /****** PIE CHART ******/
        pieChart =  findViewById(R.id.piechart_1);
        SegmentedGroup choixGraphe = findViewById(R.id.choixGraphe);
        choixGraphe.setTintColor(getResources().getColor(R.color.colorIcons), getResources().getColor(R.color.colorPrimary));
        final GridView mGridView = findViewById(R.id.gridview_1);
        DBHelper_Budget DB_Budget = new DBHelper_Budget(this);

        cursor = DB_Budget.getAllData();


        CustomAdapterGraph customAdapter = new CustomAdapterGraph(this,cursor);
        mGridView.setAdapter(customAdapter);

        /****** RADAR CHART ******/
        cursor.moveToFirst();
        final String [] categories = new String[cursor.getCount()];
        if(cursor.getCount()>0){
            int i = 0;
            do{
                categories[i] = cursor.getString(1);
                i=i+1;
            }while(cursor.moveToNext());
        }

        radarChart = (RadarChart) findViewById(R.id.radar_graph);
        radarChart.setBackgroundColor(r_BackGround);
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(r_Web);
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(r_innerWeb);
        radarChart.setWebAlpha(100);
        setData_Radar();
        radarChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setTextSize(r_TextSize);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private String[] mActivities = categories;
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mActivities[(int) value % mActivities.length];
            }
        });

        xAxis.setTextColor(r_TextColor);
        YAxis yAxis = radarChart.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(80f);
        yAxis.setDrawLabels(false);

        Legend l = radarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setTextColor(Color.WHITE);
        l.setTextSize(20f);
        radarChart.setVisibility(View.INVISIBLE);

        /****** BAR CHART ******/
        barChart = (BarChart) findViewById(R.id.bar_graph);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);

        // if more than 40 entries are displayed in the chart, no values will be drawn
        barChart.setMaxVisibleValueCount(40);

        // scaling x and y at the same time
        barChart.setPinchZoom(true);

        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setDrawLabels(false);

        IAxisValueFormatter custom = new MyAxisValueFormatter(getApplicationContext());
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f);

        Legend l2 = barChart.getLegend();
        l2.setEnabled(false);

        BarMarkerView mv = new BarMarkerView(this, custom, DB_Budget);
        mv.setChartView(barChart);
        barChart.setMarker(mv); // Set the marker to the chart

        setData_Bar();
        barChart.setVisibility(View.INVISIBLE);
        barChart_Textview = findViewById(R.id.textView4);
        barChart_Textview.setVisibility(View.INVISIBLE);


        /****** END OF CHARTS ******/

        Toolbar toolbar = findViewById(R.id.toolbar_1);
        setSupportActionBar(toolbar);

        graph_button_1 = findViewById(R.id.choix_graph_1);
        graph_button_2 = findViewById(R.id.choix_graph_2);
        graph_button_3 = findViewById(R.id.choix_graph_3);

        graph_button_1.setChecked(true);
        graph_button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridView.setVisibility(View.VISIBLE);
                radarChart.setVisibility(View.INVISIBLE);
                barChart.setVisibility(View.INVISIBLE);
                barChart_Textview.setVisibility(View.INVISIBLE);
            }
        });
        graph_button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGridView.setVisibility(View.INVISIBLE);
                radarChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.INVISIBLE);
                barChart_Textview.setVisibility(View.INVISIBLE);
            }
        });
        graph_button_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridView.setVisibility(View.INVISIBLE);
                radarChart.setVisibility(View.INVISIBLE);
                barChart.setVisibility(View.VISIBLE);
                barChart_Textview.setVisibility(View.VISIBLE);
            }
        });

        for (IDataSet<?> set : radarChart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());

        radarChart.invalidate();
    }

    public void setData_Bar(){

        cursor.moveToFirst();
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        int count = 1;
        if(cursor.getCount()>0){
            do{
                float expent = Float.parseFloat(cursor.getString(2))-Float.parseFloat(cursor.getString(3));
                entries.add(new BarEntry(count,expent));
                count ++;
            }while(cursor.moveToNext());
        }
        else return;

        BarDataSet set1;
        set1 = new BarDataSet(entries, "");
        set1.setDrawIcons(false);
        set1.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.invalidate();
    }

    public void setData_Radar() {

        cursor.moveToFirst();
        ArrayList<RadarEntry> entries2 = new ArrayList<RadarEntry>();

        if(cursor.getCount()>0){
            do{
                float remainingPercent = Float.parseFloat(cursor.getString(3))/Float.parseFloat(cursor.getString(2));
                entries2.add(new RadarEntry(remainingPercent*100.0f));
            }while(cursor.moveToNext());
        }
        else return;

        RadarDataSet set2 = new RadarDataSet(entries2, getString(R.string.left)+" Budget %");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set2);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        radarChart.setData(data);
        radarChart.invalidate();
    }
}
