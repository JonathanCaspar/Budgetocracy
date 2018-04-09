package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

/**
 * Created by Max on 2018-04-06.
 */

public class BarMarkerView extends MarkerView {

    private TextView tvContent;
    Cursor cursor;

    public BarMarkerView(Context context, IAxisValueFormatter xAxisValueFormatter, DBHelper_Budget DB_Budget) {
        super(context, R.layout.bar_makerview);
        tvContent = (TextView) findViewById(R.id.tvContent);
        cursor = DB_Budget.getAllData();
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        cursor.moveToFirst();
        cursor.move((int)e.getX()-1);
        tvContent.setText(cursor.getString(1));

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}