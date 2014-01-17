package ch.trillian.todo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import ch.trillian.todo.contentprovider.TodoContentProvider;
import ch.trillian.todo.database.TodoTable;

/*
 * TodoDetailActivity allows to enter a new todo item 
 * or to change an existing one
 */
public class TodoDetailActivity extends Activity {
  
  private Spinner categoryView;
  private EditText summaryView;
  private EditText descriptionView;

  private Uri todoUri;

  @Override
  protected void onCreate(Bundle bundle) {
    
    super.onCreate(bundle);
    setContentView(R.layout.activity_todo_detail);

    categoryView = (Spinner) findViewById(R.id.category);
    summaryView = (EditText) findViewById(R.id.todo_edit_summary);
    descriptionView = (EditText) findViewById(R.id.todo_edit_description);

    // try to set URI from saved state
    todoUri = (bundle == null) ? null : (Uri) bundle.getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);

    // try to set URI from intent
    if (todoUri == null) {
      Bundle extras = getIntent().getExtras();
      todoUri = extras == null ? null : (Uri) extras.getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);
    }

    // fill data if URI is set
    if (todoUri != null) {
      readTodo(todoUri);
    }
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    
    super.onSaveInstanceState(outState);
    outState.putParcelable(TodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.todo_detail, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
    case R.id.action_save:
      saveTodo();
      finish();
      return true;
    case R.id.action_share:
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "TODO: " + summaryView.getText().toString());
      intent.putExtra(android.content.Intent.EXTRA_TEXT, descriptionView.getText().toString());
      startActivity(intent); 
      return true;      
    case R.id.action_delete:
      deleteTodo();
      Toast.makeText(TodoDetailActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
      finish();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  private void readTodo(Uri uri) {
    
    String[] projection = { TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_CATEGORY };
    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    
    if (cursor != null) {
      
      if(cursor.moveToFirst()) {
        categoryView.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_CATEGORY)));
        summaryView.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_SUMMARY)));
        descriptionView.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_DESCRIPTION)));
      }

      cursor.close();
    }
  }

  private void saveTodo() {
    
    int category = categoryView.getSelectedItemPosition();
    String summary = summaryView.getText().toString();
    String description = descriptionView.getText().toString();

    // only save if either summary or description is set
    if (description.length() == 0 && summary.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();
    values.put(TodoTable.COLUMN_CATEGORY, category);
    values.put(TodoTable.COLUMN_SUMMARY, summary);
    values.put(TodoTable.COLUMN_DESCRIPTION, description);

    // insert or update item
    if (todoUri == null) {
      todoUri = getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
    } else {
      getContentResolver().update(todoUri, values, null, null);
    }
  }

  private void deleteTodo() {
    
    if (todoUri != null) {
      getContentResolver().delete(todoUri, null, null);
    }
  }
}