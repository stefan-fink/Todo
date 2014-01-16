package ch.trillian.todo.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import ch.trillian.todo.database.TodoDatabaseHelper;
import ch.trillian.todo.database.TodoTable;

public class TodoContentProvider extends ContentProvider {

  // database
  private TodoDatabaseHelper databaseHelper;

  // used for the UriMacher
  private static final int TODOS = 10;
  private static final int TODO_ID = 20;

  private static final String AUTHORITY = "ch.trillian.todo.contentprovider";
  private static final String BASE_PATH = "todos";
  
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
  }

  @Override
  public boolean onCreate() {
    databaseHelper = new TodoDatabaseHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    Log.w("TODO", "TodoContentProvider: " + uri);
    
	  // Using SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // check if the caller has requested a column which does not exists
    checkColumns(projection);

    // Set the table
    queryBuilder.setTables(TodoTable.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case TODOS:
      break;
    case TODO_ID:
      // adding the ID to the original query
      queryBuilder.appendWhere(TodoTable.COLUMN_ID + "=" + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    
    // make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    
    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    long id;

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case TODOS:
      id = database.insert(TodoTable.TABLE_NAME, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    
    getContext().getContentResolver().notifyChange(uri, null);
    
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    
    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    int rowsDeleted = 0;

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case TODOS:
      rowsDeleted = database.delete(TodoTable.TABLE_NAME, selection, selectionArgs);
      break;
    case TODO_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsDeleted = database.delete(TodoTable.TABLE_NAME, TodoTable.COLUMN_ID + "=" + id, null);
      } else {
        rowsDeleted = database.delete(TodoTable.TABLE_NAME, TodoTable.COLUMN_ID + "=" + id + " and " + selection,  selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    
    getContext().getContentResolver().notifyChange(uri, null);
    
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    int rowsUpdated = 0;

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case TODOS:
      rowsUpdated = database.update(TodoTable.TABLE_NAME, values, selection, selectionArgs);
      break;
    case TODO_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = database.update(TodoTable.TABLE_NAME, values, TodoTable.COLUMN_ID + "=" + id, null);
      } else {
        rowsUpdated = database.update(TodoTable.TABLE_NAME, values, TodoTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    
    getContext().getContentResolver().notifyChange(uri, null);
    
    return rowsUpdated;
  }

  private void checkColumns(String[] projection) {
    
    String[] available = { TodoTable.COLUMN_CATEGORY, TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_ID };
    
    // check if all columns which are requested are available
    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException("Unknown columns in projection");
      }
    }
  }
}
