package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper{

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_PRICE + " TEXT, " +
                InventoryEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0 NOT NULL, " +
                InventoryEntry.COLUMN_IMAGE + " BLOB, " +
                InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";
        Log.v(LOG_TAG,SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
