package projet.ift2905.budgetocracy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DBHelper_Budget extends SQLiteOpenHelper {

    public static final String DATABASE_NAME_BUDGET = "budget.db";
    public static final String TABLE_NAME = "budget_table";
    public static final String COL_BUDGET_0 = "ID";
    public static final String COL_BUDGET_1 = "NAME";
    public static final String COL_BUDGET_2 = "AMOUNT";
    public static final String COL_BUDGET_3 = "REMAINING";

    public DBHelper_Budget(Context context) {
        super(context, DATABASE_NAME_BUDGET, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " +TABLE_NAME+ "( "
                + COL_BUDGET_0 +" INTEGER PRIMARY KEY AUTOINCREMENT  , "
                + COL_BUDGET_1 +" TEXT , "
                + COL_BUDGET_2 +" REAL , "
                + COL_BUDGET_3 +" REAL ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    /**** EXEMPLE INSERT  *****/
    public long insertDataName(String name, float amount, float remaining){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_1,name);
        contentValues.put(COL_BUDGET_2,amount);
        contentValues.put(COL_BUDGET_3,remaining);
        long id = db.insert(TABLE_NAME,null,contentValues);
        return id;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME,null);
    }

    public String getAllStringData(){
        /** Cursor is the pointer that traverse the data*/
        Cursor result = this.getAllData();

        /** Buffer will stock the data filtred from the DATABASE*/
        StringBuffer buffer = new StringBuffer();
        while (result.moveToNext()) {
            buffer.append("ID: " + result.getString(0) + " - ");
            buffer.append("NAME: " + result.getString(1) + " - ");
            buffer.append("BUDGET: " + result.getString(2) + " - ");
            buffer.append("REMAINING: " + result.getString(3) + "\n\n");
        }
        return buffer.toString();
    }

    public HashMap<Integer,String> getBudgetList(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.rawQuery("select "+ COL_BUDGET_0+", "+ COL_BUDGET_1 +" from "+TABLE_NAME,null);

        HashMap<Integer,String> hm = new HashMap<Integer,String>();

        while (data.moveToNext()) {
            // ID & NAME
            hm.put(data.getInt(0),data.getString(1));
        }
        return hm;
    }

    public Boolean updateData(String id,String name, float amount, float remaining ){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_0,id);
        contentValues.put(COL_BUDGET_1,name);
        contentValues.put(COL_BUDGET_2,amount);
        contentValues.put(COL_BUDGET_3,remaining);
        db.update(TABLE_NAME,contentValues,"ID = ?",new String[] {id});
        return true;
    }

    public void updateRemainingAmount(Integer ID, Float toSubstract){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE "+TABLE_NAME+" SET "+COL_BUDGET_3+"="+COL_BUDGET_3+"-"+toSubstract+" WHERE id="+ID);
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

    public void deleteDataBase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_NAME); //delete all rows in a table
    }

}
