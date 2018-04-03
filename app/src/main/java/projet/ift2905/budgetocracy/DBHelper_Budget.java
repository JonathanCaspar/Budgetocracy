package projet.ift2905.budgetocracy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Max on 2018-03-15.
 */


// #######################MODIF COL_BUDGET_1 = "_id"

public class DBHelper_Budget extends SQLiteOpenHelper {

    public static final String DATABASE_NAME_BUDGET = "budget.db";
    public static final String TABLE_NAME_BUDGET = "budget_table";
    public static final String COL_BUDGET_1 = "_id";
    public static final String COL_BUDGET_2 = "NAME";
    public static final String COL_BUDGET_3 = "AMOUNT";
    public static final String COL_BUDGET_4 = "REMAINING";

    public DBHelper_Budget(Context context) {
        super(context, DATABASE_NAME_BUDGET, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " +TABLE_NAME_BUDGET+ "( "
                +COL_BUDGET_1+" INTEGER PRIMARY KEY AUTOINCREMENT  , "
                +COL_BUDGET_2+" TEXT , "
                +COL_BUDGET_3+" REAL , "
                +COL_BUDGET_4+" REAL ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+TABLE_NAME_BUDGET;
        db.execSQL(sql);
        onCreate(db);
    }

    /**** EXEMPLE INSERT  *****/
    public boolean insertDataName(String name, float amount, float remaining){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_2,name);
        contentValues.put(COL_BUDGET_3,amount);
        contentValues.put(COL_BUDGET_4,remaining);
        long result = db.insert(TABLE_NAME_BUDGET,null,contentValues);
        if (result==-1) return false;
        return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME_BUDGET,null);
    }

    public Boolean updateData(String id,String name, float amount, float remaining ){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_1,id);
        contentValues.put(COL_BUDGET_2,name);
        contentValues.put(COL_BUDGET_3,amount);
        contentValues.put(COL_BUDGET_4,remaining);
        db.update(TABLE_NAME_BUDGET,contentValues,"ID = ?",new String[] {id});
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_BUDGET, "ID = ?",new String[] {id});
    }

    public boolean deleteDataBase (Context context) {
        context.deleteDatabase(DATABASE_NAME_BUDGET);
        return true;
    }





}