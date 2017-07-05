package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    //An empty private constructor makes sure that the class is not going to be initialised.
    private InventoryContract(){
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_EMAIL = "email";

        /**
         * Possible inventory suppliers.
         */
        public static final int SUPPLIER_QUILL_LONDON = 0;
        public static final int SUPPLIER_SCRIBBLERS = 1;
        public static final int SUPPLIER_CULT_PENS = 2;
        public static final int SUPPLIER_JET_PENS = 3;
        public static final int SUPPLIER_UNKNOWN = 4;

        /**
         * Returns whether or not the given supplier is {@link #SUPPLIER_QUILL_LONDON}, {@link #SUPPLIER_SCRIBBLERS},
         * {@link #SUPPLIER_CULT_PENS}, {@link #SUPPLIER_JET_PENS}, or {@link #SUPPLIER_UNKNOWN}.
         */
        public static boolean isValidSupplier(int supplier) {
            if (supplier == SUPPLIER_QUILL_LONDON || supplier == SUPPLIER_SCRIBBLERS || supplier == SUPPLIER_CULT_PENS || supplier == SUPPLIER_JET_PENS || supplier == SUPPLIER_UNKNOWN) {
                return true;
            }
            return false;
        }

    }

}
