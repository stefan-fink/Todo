package ch.trillian.todo.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TodoTable {

  public static final String TABLE_NAME = "todo";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_CATEGORY = "category";
  public static final String COLUMN_SUMMARY = "summary";
  public static final String COLUMN_DESCRIPTION = "description";

  private static final String DATABASE_CREATE = "create table " 
      + TABLE_NAME
      + "(" 
      + COLUMN_ID + " integer primary key autoincrement, " 
      + COLUMN_CATEGORY + " integer not null, " 
      + COLUMN_SUMMARY + " text not null," 
      + COLUMN_DESCRIPTION + " text not null" 
      + ");";

  public static void onCreate(SQLiteDatabase database) {
    
    database.execSQL(DATABASE_CREATE);
  }

  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    
    Log.w(TodoTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
    database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(database);
  }
}
