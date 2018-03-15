package projet.ift2905.budgetocracy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Max on 2018-03-15.
 */

public class DBHelper_Expenses extends SQLiteOpenHelper {

    public static final String DATABASE_NAME_EXPENSES = "expenses.db";
    public static final String TABLE_NAME_EXPENSES = "expenses_table";
    public static final String COL_EXPENSES_1 = "ID";
    public static final String COL_EXPENSES_2 = "NAME";
    public static final String COL_EXPENSES_3 = "AMOUNT";
    public static final String COL_EXPENSES_4 = "DATE";


    public DBHelper_Expenses(Context context) {
        super(context, DATABASE_NAME_EXPENSES, null, 1);
        SQLiteDatabase db = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table" +TABLE_NAME_EXPENSES+ "( "
                +COL_EXPENSES_1+" INTEGER PRIMARY KEY AUTOINCREMENT  , "
                +COL_EXPENSES_2+" TEXT , "
                +COL_EXPENSES_3+" FLOAT , "
                +COL_EXPENSES_4+" INTEGER ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME_EXPENSES;
        db.execSQL(sql);
        onCreate(db);
    }
}
