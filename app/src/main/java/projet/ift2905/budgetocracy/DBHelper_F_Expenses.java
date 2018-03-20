package projet.ift2905.budgetocracy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Max on 2018-03-15.
 */


public class DBHelper_F_Expenses extends SQLiteOpenHelper {

    public static final String DATABASE_NAME_F_EXPENSES = "F_expenses.db";
    public static final String TABLE_NAME_F_EXPENSES = "F_expenses_table";
    public static final String COL_F_EXPENSES_1 = "ID";
    public static final String COL_F_EXPENSES_2 = "NAME";
    public static final String COL_F_EXPENSES_3 = "AMOUNT";
    public static final String COL_F_EXPENSES_4 = "DATE";
    public static final String COL_F_EXPENSES_5 = "FREQUENCY";
    public static final String COL_F_EXPENSES_6 = "PERIOD";


    public DBHelper_F_Expenses(Context context) {
        super(context, DATABASE_NAME_F_EXPENSES, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table" +TABLE_NAME_F_EXPENSES+ "( "
                +COL_F_EXPENSES_1+" INTEGER PRIMARY KEY AUTOINCREMENT  , "
                +COL_F_EXPENSES_2+" TEXT , "
                +COL_F_EXPENSES_3+" FLOAT , "
                +COL_F_EXPENSES_4+" TEXT , "
                +COL_F_EXPENSES_5+" INTEGER , "
                +COL_F_EXPENSES_6+" INTEGER ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME_F_EXPENSES;
        db.execSQL(sql);
        onCreate(db);
    }

    /**** EXEMPLE INSERT  *****/
    public boolean insertDataName(String name, float amount, String date, int frequency, int period){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_F_EXPENSES_2,name);
        contentValues.put(COL_F_EXPENSES_3,amount);
        contentValues.put(COL_F_EXPENSES_4,date);
        contentValues.put(COL_F_EXPENSES_5,frequency);
        contentValues.put(COL_F_EXPENSES_6,period);
        long result = db.insert(TABLE_NAME_F_EXPENSES,null,contentValues);
        if (result==-1) return false;
        return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME_F_EXPENSES,null);
    }

    public Boolean updateData(String id,String name, float amount, String date, int frequency, int period ){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_F_EXPENSES_1,id);
        contentValues.put(COL_F_EXPENSES_2,name);
        contentValues.put(COL_F_EXPENSES_3,amount);
        contentValues.put(COL_F_EXPENSES_4,date);
        contentValues.put(COL_F_EXPENSES_5,frequency);
        contentValues.put(COL_F_EXPENSES_6,period);
        db.update(TABLE_NAME_F_EXPENSES,contentValues,"ID = ?",new String[] {id});
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_F_EXPENSES, "ID = ?",new String[] {id});
    }

    public boolean deleteDataBase (Context context) {
        context.deleteDatabase(DATABASE_NAME_F_EXPENSES);
        return true;
    }
}
