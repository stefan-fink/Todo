package ch.trillian.todo;

import ch.trillian.todo.contentprovider.TodoContentProvider;
import ch.trillian.todo.database.TodoTable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class TodosOverviewActivity extends ListActivity implements LoaderCallbacks<Cursor> {
  
  private static final int ACTIVITY_CREATE = 0;
  private static final int ACTIVITY_EDIT = 1;
  private static final int DELETE_ID = Menu.FIRST + 1;
  
  // private Cursor cursor;
  private SimpleCursorAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.todo_list);
    fillData();
    registerForContextMenu(getListView());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.todos_overview, menu);
    
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    
    switch (item.getItemId()) {
    case R.id.action_new:
      createTodo();
      return true;
    }
    
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    
    super.onCreateContextMenu(menu, v, menuInfo);
    
    if (v == getListView()) {
      menu.add(0, DELETE_ID, 0, R.string.action_delete);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    
    switch (item.getItemId()) {
    case DELETE_ID:
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      Uri uri = Uri.parse(TodoContentProvider.CONTENT_URI + "/" + info.id);
      getContentResolver().delete(uri, null, null);
      return true;
    }
    
    return super.onContextItemSelected(item);
  }

  private void createTodo() {
    
    Intent intent = new Intent(this, TodoDetailActivity.class);
    startActivity(intent);
  }

  // Opens the second activity if an entry is clicked
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    
    super.onListItemClick(l, v, position, id);
    
    Intent intent = new Intent(this, TodoDetailActivity.class);
    Uri todoUri = Uri.parse(TodoContentProvider.CONTENT_URI + "/" + id);
    intent.putExtra(TodoContentProvider.CONTENT_ITEM_TYPE, todoUri);

    startActivity(intent);
  }


  private void fillData() {

    // Fields from the database (projection)
    // Must include the _id column for the adapter to work
    String[] from = new String[] { TodoTable.COLUMN_SUMMARY };
    // Fields on the UI to which we map
    int[] to = new int[] { R.id.label };

    getLoaderManager().initLoader(0, null, this);
    adapter = new SimpleCursorAdapter(this, R.layout.todo_row, null, from, to, 0);

    setListAdapter(adapter);
  }

  // creates a new loader after the initLoader() call
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] projection = { TodoTable.COLUMN_ID, TodoTable.COLUMN_SUMMARY };
    CursorLoader cursorLoader = new CursorLoader(this, TodoContentProvider.CONTENT_URI, projection, null, null, null);
    return cursorLoader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    adapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    // data is not available anymore, delete reference
    adapter.swapCursor(null);
  }

}
