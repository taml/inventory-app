package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 0;
    InventoryCursorAdapter mInventoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Find GridView to populate
        GridView gridViewItems = (GridView) findViewById(R.id.inventory_grid_view);
        // Setup cursor adapter using cursor from last step
        mInventoryAdapter = new InventoryCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        gridViewItems.setAdapter(mInventoryAdapter);

        // Start item onclick listener
        gridViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent addEditIntent = new Intent(InventoryActivity.this, AddEditActivity.class);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                addEditIntent.setData(currentItemUri);
                startActivity(addEditIntent);
            }
        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    private void insertInventoryItem(){
        // Create a ContentValues object where column names are the keys,
        // and calligraphy nib attributes are the item values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, "Calligraphy Nib");
        values.put(InventoryEntry.COLUMN_PRICE, 0.89);
        values.put(InventoryEntry.COLUMN_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_SCRIBBLERS);
        values.put(InventoryEntry.COLUMN_SUPPLIER_EMAIL, "scribblers@example.com");

        // Insert a new row for calligraphy nibs into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the inventory database table.
        // Receive the new content URI that will allow us to access the calligraphy nibs data in the future.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Add" menu option
            case R.id.add_item:
//                insertInventoryItem();
                Intent addItemIntent = new Intent(InventoryActivity.this, AddEditActivity.class);
                startActivity(addItemIntent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_all_items:
                deleteAllInventoryItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryEntry._ID, InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE, InventoryEntry.COLUMN_QUANTITY};
        return new CursorLoader(this, InventoryEntry.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryAdapter.swapCursor(null);
    }

    private void deleteAllInventoryItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from inventory database");
    }
}
