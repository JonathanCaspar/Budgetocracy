package projet.ift2905.budgetocracy;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.RadioButton;

import com.github.mikephil.charting.charts.PieChart;

import info.hoang8f.android.segmented.SegmentedGroup;

public class GraphicActivity extends AppCompatActivity {

    PieChart pieChart;
    RadioButton graph_button_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.graphics);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_1));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        pieChart =  findViewById(R.id.piechart_1);
        //ListView mListView = findViewById(R.id.l_view_graph);
        SegmentedGroup choixGraphe = findViewById(R.id.choixGraphe);
        choixGraphe.setTintColor(getResources().getColor(R.color.colorIcons), getResources().getColor(R.color.colorPrimary));

        GridView mGridView = findViewById(R.id.gridview_1);
        DBHelper_Budget DB_Budget = new DBHelper_Budget(this);

        Cursor cursor = DB_Budget.getAllData();
        CustomAdapterGraph customAdapter = new CustomAdapterGraph(this,cursor);
        mGridView.setAdapter(customAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar_1);
        setSupportActionBar(toolbar);

        graph_button_1 = findViewById(R.id.choix_graph_1);

        graph_button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Base de données - Budget :", "yolo");
            }
        });
    }


    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.graphics_bar_menu, menu);
    //    return true;
    //}

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
        // PRE SETTINGS
        /*
        pieChart = (PieChart) findViewById(R.id.piechart_1);

        pieChart.setRotationEnabled(true);

        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(65.f);
        pieChart.setTransparentCircleRadius(75f);

        pieChart.setDrawCenterText(true);
        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        // TEXT FROM THE MIDDLE
        SpannableString s = new SpannableString("Budget: 80.0$\nLeft: 25.0$");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
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
        */

}
