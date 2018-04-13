package projet.ift2905.budgetocracy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper_Expenses extends SQLiteOpenHelper {

    public static final String DATABASE_NAME_EXPENSES = "expenses.db";
    public static final String TABLE_NAME = "expenses_table";
    public static final String COL_EXPENSES_1 = "_id";
    public static final String COL_EXPENSES_2 = "NAME";
    public static final String COL_EXPENSES_3 = "CATEGORY_ID";
    public static final String COL_EXPENSES_4 = "AMOUNT";
    public static final String COL_EXPENSES_5 = "DATE";


    public DBHelper_Expenses(Context context) {
        super(context, DATABASE_NAME_EXPENSES, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                +COL_EXPENSES_1+" INTEGER PRIMARY KEY AUTOINCREMENT  , "
                +COL_EXPENSES_2+" TEXT , "
                +COL_EXPENSES_3+" INTEGER , "
                +COL_EXPENSES_4+" FLOAT , "
                +COL_EXPENSES_5+" TEXT ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS "+ TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    /**** EXEMPLE INSERT  *****/
    public boolean insertDataName(String name, Integer category, float amount, String date){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EXPENSES_2,name);
        contentValues.put(COL_EXPENSES_3,category);
        contentValues.put(COL_EXPENSES_4,amount);
        contentValues.put(COL_EXPENSES_5,date);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result==-1) return false;
        return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+ TABLE_NAME,null);
    }

    public String getAllStringData(){
        /** Cursor is the pointer that traverse the data*/
        Cursor result = this.getAllData();

        /** Buffer will stock the data filtred from the DATABASE*/
        StringBuffer buffer = new StringBuffer();
        while (result.moveToNext()) {
            buffer.append("ID: " + result.getString(0) + " - ");
            buffer.append("NAME: " + result.getString(1) + " - ");
            buffer.append("CATEGORY_ID: " + String.valueOf(result.getInt(2)) + " - ");
            buffer.append("AMOUNT: " + result.getString(3) + " - ");
            buffer.append("DATE: " + result.getString(4) + "\n\n");
        }
        return buffer.toString();
    }

    public Boolean updateData(String id,String name, Integer categoryID, float amount, String date ){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EXPENSES_1,id);
        contentValues.put(COL_EXPENSES_2,name);
        contentValues.put(COL_EXPENSES_3,categoryID);
        contentValues.put(COL_EXPENSES_4,amount);
        contentValues.put(COL_EXPENSES_5,date);
        db.update(TABLE_NAME,contentValues,"_id = ?",new String[] {id});
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "_id = ?",new String[] {id});
    }

    public void deleteDataBase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_NAME); //delete all rows in a table
    }




    public Cursor getExpenseListByKeyword (String search, EnumSort sort){
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery="";
        switch (sort.getSort()){
            case sortByDate:
                selectQuery =  "SELECT  rowid as " +
                        DBHelper_Expenses.COL_EXPENSES_1 + "," +
                        DBHelper_Expenses.COL_EXPENSES_2 + "," +
                        DBHelper_Expenses.COL_EXPENSES_3 + "," +
                        DBHelper_Expenses.COL_EXPENSES_4 + "," +
                        DBHelper_Expenses.COL_EXPENSES_5 +
                        " FROM " + DBHelper_Expenses.TABLE_NAME+
                        " WHERE " +  DBHelper_Expenses.COL_EXPENSES_2 + "  LIKE  '%" +search + "%' "+
                        " ORDER BY " + DBHelper_Expenses.COL_EXPENSES_5
                        ;

                break;
            case sortByAmount:
                selectQuery =  "SELECT  rowid as " +
                        DBHelper_Expenses.COL_EXPENSES_1 + "," +
                        DBHelper_Expenses.COL_EXPENSES_2 + "," +
                        DBHelper_Expenses.COL_EXPENSES_3 + "," +
                        DBHelper_Expenses.COL_EXPENSES_4 + "," +
                        DBHelper_Expenses.COL_EXPENSES_5 +
                        " FROM " + DBHelper_Expenses.TABLE_NAME+
                        " WHERE " +  DBHelper_Expenses.COL_EXPENSES_2 + "  LIKE  '%" +search + "%' "+
                        " ORDER BY " + DBHelper_Expenses.COL_EXPENSES_4
                        ;

                break;

            case sortByName:
                selectQuery =  "SELECT  rowid as " +
                        DBHelper_Expenses.COL_EXPENSES_1 + "," +
                        DBHelper_Expenses.COL_EXPENSES_2 + "," +
                        DBHelper_Expenses.COL_EXPENSES_3 + "," +
                        DBHelper_Expenses.COL_EXPENSES_4 + "," +
                        DBHelper_Expenses.COL_EXPENSES_5 +
                        " FROM " + DBHelper_Expenses.TABLE_NAME+
                        " WHERE " +  DBHelper_Expenses.COL_EXPENSES_2 + "  LIKE  '%" +search + "%' "+
                        " ORDER BY " + DBHelper_Expenses.COL_EXPENSES_2
                        ;

                break;

            default :
                break;



        }

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


    public String getNameExpenseWithID (String id) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "";

        selectQuery = "SELECT " +
                DBHelper_Expenses.COL_EXPENSES_2 +
                " FROM " + DBHelper_Expenses.TABLE_NAME +
                " WHERE _id = " + id
        ;
        //return db.execSQL(selectQuery);
        Cursor cursor =  db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getString(0);

    }

    public String getCategorieExpenseWithID (String id) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "";

        selectQuery = "SELECT " +
                DBHelper_Expenses.COL_EXPENSES_3 +
                " FROM " + DBHelper_Expenses.TABLE_NAME  +
                " WHERE _id = " + id
        ;
        //return db.execSQL(selectQuery);
        Cursor cursor =  db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getString(0);

    }


    public String getAmountExpenseWithID (String id) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "";

        selectQuery = "SELECT " +
                DBHelper_Expenses.COL_EXPENSES_4 +
                " FROM " + DBHelper_Expenses.TABLE_NAME  +
                " WHERE _id = " + id
        ;
        //return db.execSQL(selectQuery);
        Cursor cursor =  db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getString(0);

    }

    public String getDateExpenseWithID (String id) {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "";

        selectQuery = "SELECT " +
                DBHelper_Expenses.COL_EXPENSES_5 +
                " FROM " + DBHelper_Expenses.TABLE_NAME  +
                " WHERE _id = " + id
        ;
        //return db.execSQL(selectQuery);
        Cursor cursor =  db.rawQuery(selectQuery, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor.getString(0);

    }


    public Cursor getExpensesAssociateToBudget (String idBudget){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery="";

        selectQuery = "SELECT * FROM " + DBHelper_Expenses.TABLE_NAME  +
                " WHERE "+ DBHelper_Expenses.COL_EXPENSES_3 +"=" + idBudget
        ;

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



}