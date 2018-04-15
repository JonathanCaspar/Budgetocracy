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
    public static final String COL_BUDGET_0 = "_id";
    public static final String COL_BUDGET_1 = "NAME";
    public static final String COL_BUDGET_2 = "AMOUNT";
    public static final String COL_BUDGET_3 = "REMAINING";

    private static DBHelper_Budget sInstance;

    public DBHelper_Budget(Context context) {
        super(context, DATABASE_NAME_BUDGET, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_NAME + "( "
                + COL_BUDGET_0 + " INTEGER PRIMARY KEY AUTOINCREMENT  , "
                + COL_BUDGET_1 + " TEXT , "
                + COL_BUDGET_2 + " REAL , "
                + COL_BUDGET_3 + " REAL ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    /**** EXEMPLE INSERT  *****/
    public long insertDataName(String name, float amount, float remaining) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_1, name);
        contentValues.put(COL_BUDGET_2, amount);
        contentValues.put(COL_BUDGET_3, remaining);
        long id = db.insert(TABLE_NAME, null, contentValues);
        return id;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public HashMap<Integer, String> getBudgetList() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.rawQuery("select " + COL_BUDGET_0 + ", " + COL_BUDGET_1 + " from " + TABLE_NAME, null);

        HashMap<Integer, String> hm = new HashMap<Integer, String>();

        while (data.moveToNext()) {
            // ID & NAME
            hm.put(data.getInt(0), data.getString(1));
        }
        return hm;
    }

    public Boolean updateData(String id, String name, float amount, float remaining) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BUDGET_0, id);
        contentValues.put(COL_BUDGET_1, name);
        contentValues.put(COL_BUDGET_2, amount);
        contentValues.put(COL_BUDGET_3, remaining);
        db.update(TABLE_NAME, contentValues, "_id = ?", new String[]{id});
        return true;
    }

    public void substractRemainingAmount(Integer ID, Float toSubstract) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COL_BUDGET_3 + "=" + COL_BUDGET_3 + "-" + toSubstract + " WHERE _id=" + ID);
    }

    public void increaseRemainingAmount(Integer ID, Float toSubstract) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COL_BUDGET_3 + "=" + COL_BUDGET_3 + "+" + toSubstract + " WHERE _id=" + ID);
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "_id = ?", new String[]{id});
    }

    public void deleteDataBase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME); //delete all rows in a table
        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='budget_table'"); //reset the primary keys
    }

    public String getStringBudgetWithID(String id) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " +
                DBHelper_Budget.COL_BUDGET_1 +
                " FROM " + DBHelper_Budget.TABLE_NAME +
                " WHERE _id = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getString(0);

    }

    public void resetRemaining() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COL_BUDGET_3 + "=" + COL_BUDGET_2);

    }

    public void updateRemaining(Cursor expenses) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Mets à jour les remainings pour les dépenses ayant le même mois courant
        while (expenses.moveToNext()) {
            String expenseDate = expenses.getString(4).split("-")[1];
            if (MainActivity.isSameMonthAsCurrent(expenseDate)) {
                db.execSQL("UPDATE " + TABLE_NAME + " SET " + COL_BUDGET_3 + "=" + COL_BUDGET_2 + "-" + expenses.getFloat(3) + " WHERE _id=" + expenses.getString(2));
            }
        }
    }

    public Cursor getBudget(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_BUDGET_0 + "=" + id;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public Float getRemaining(String idExpense) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " +
                DBHelper_Budget.COL_BUDGET_3 +
                " FROM " + DBHelper_Budget.TABLE_NAME +
                " WHERE _id = " + idExpense;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getFloat(0);
    }
}
