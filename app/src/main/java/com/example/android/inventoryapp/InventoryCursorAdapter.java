package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter{

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank grid item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * grid item layout. For example, the name for the current item can be set on the name TextView
     * in the grid item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final Button saleButton  = (Button) view.findViewById(R.id.item_sale_button);
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        String name = cursor.getString(nameColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);

        nameTextView.setText(name);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);

        final int inventoryItemId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        final int inventoryItemQuantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inventoryItemQuantity > 0) {
                   int quantity = inventoryItemQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
                    Uri newUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, inventoryItemId);
                    context.getContentResolver().update(newUri, values, null, null);
                }
                else {
                    Toast.makeText(context, R.string.no_quantity, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
