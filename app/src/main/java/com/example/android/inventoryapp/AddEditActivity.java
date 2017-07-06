package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class AddEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 0;
    private ImageView mItemImageView;
    private Button mItemPicButton;
    private EditText mItemNameEditEditText;
    private EditText mItemPriceEditText;
    private SeekBar mItemQuantitySeekBar;
    private TextView mTotalQuantityTextView;
    private Spinner mItemSupplierNameSpinner;
    private EditText mItemSupplierEmailEditText;
    private Button mOrderMoreButton;

    private int mSupplier = 4;
    private int mQuantity = 0;

    /** Content URI for the existing inventory item (null if it's a new item) */
    private Uri mCurrentInventoryItemUri;

    private boolean mInventoryItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        mItemImageView = (ImageView) findViewById(R.id.item_image);
        mItemPicButton = (Button) findViewById(R.id.add_item_picture_button);
        mItemNameEditEditText = (EditText) findViewById(R.id.item_name_text);
        mItemPriceEditText = (EditText) findViewById(R.id.item_price_text);
        mItemQuantitySeekBar = (SeekBar) findViewById(R.id.quantity_seekbar);
        mTotalQuantityTextView = (TextView) findViewById(R.id.total_quantity);
        mItemSupplierNameSpinner = (Spinner) findViewById(R.id.supplier_spinner);
        mItemSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email_text);
        mOrderMoreButton = (Button) findViewById(R.id.order_more_button);

        Intent inventoryIntent = getIntent();
        mCurrentInventoryItemUri = inventoryIntent.getData();

        if (mCurrentInventoryItemUri == null) {
            setTitle(getString(R.string.add_an_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_an_item));
            Log.v("AddEditActivity", "ID: " + mCurrentInventoryItemUri );
        }

        setupSpinner();
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        mItemQuantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mQuantity = progress;
                mTotalQuantityTextView.setText("Total Quantity: " + mQuantity + " / 100.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(AddEditActivity.this, "Slide the quantity selector up or down to select quantity", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mItemNameEditEditText.setOnTouchListener(mTouchListener);
        mItemPriceEditText.setOnTouchListener(mTouchListener);
        mItemQuantitySeekBar.setOnTouchListener(mTouchListener);
        mItemSupplierNameSpinner.setOnTouchListener(mTouchListener);
        mItemSupplierEmailEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the inventory supplier.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mItemSupplierNameSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mItemSupplierNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_one))) {
                        mSupplier = InventoryEntry.SUPPLIER_QUILL_LONDON; // Supplier Quill London
                    } else if (selection.equals(getString(R.string.supplier_two))) {
                        mSupplier = InventoryEntry.SUPPLIER_SCRIBBLERS; // Supplier Scribblers
                    } else if (selection.equals(getString(R.string.supplier_three))) {
                        mSupplier = InventoryEntry.SUPPLIER_CULT_PENS; // Supplier Cult Pens
                    } else if (selection.equals(getString(R.string.supplier_four))) {
                        mSupplier = InventoryEntry.SUPPLIER_JET_PENS; // Supplier Jet Pens
                    } else {
                        mSupplier = InventoryEntry.SUPPLIER_UNKNOWN; // Supplier Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = InventoryEntry.SUPPLIER_UNKNOWN; // Supplier Unknown
            }
        });
    }

    private void saveInventoryItem() {

        String itemName = mItemNameEditEditText.getText().toString().trim();
        String itemPrice = mItemPriceEditText.getText().toString().trim();
        String itemSupplierEmail = mItemSupplierEmailEditText.getText().toString().trim();

        if (mCurrentInventoryItemUri == null &&
                TextUtils.isEmpty(itemName) && TextUtils.isEmpty(itemPrice) &&
                TextUtils.isEmpty(itemSupplierEmail)
                && mSupplier == InventoryEntry.SUPPLIER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, itemName);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0.00 by default.
        double price = 0.00;
        if (!TextUtils.isEmpty(itemPrice)) {
            price = Double.parseDouble(itemPrice);
        }
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, mQuantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, mSupplier);
        values.put(InventoryEntry.COLUMN_SUPPLIER_EMAIL, itemSupplierEmail);

        if (mCurrentInventoryItemUri == null) {
            // Insert a new inventory item into the provider, returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.error_saving),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.success_saving),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentInventoryItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.error_updating),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.success_updating),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit_add_inventory, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new inventory item, hide the "Delete" menu item.
        if (mCurrentInventoryItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_inventory_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_item:
                saveInventoryItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_inventory_item:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mInventoryItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the inventory item hasn't changed, continue with handling back button press
        if (!mInventoryItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mCurrentInventoryItemUri == null) {
            return null;
        }
        // Since the editor shows all inventory item attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_EMAIL };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of inventory item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplierName = cursor.getInt(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mItemNameEditEditText.setText(name);
            mItemPriceEditText.setText(Double.toString(price));
            mItemQuantitySeekBar.setProgress(quantity);
            mTotalQuantityTextView.setText("Total Quantity: " + quantity + " / 100");
            mItemSupplierEmailEditText.setText(supplierEmail);
            switch (supplierName) {
                case InventoryEntry.SUPPLIER_QUILL_LONDON:
                    mItemSupplierNameSpinner.setSelection(0);
                    break;
                case InventoryEntry.SUPPLIER_SCRIBBLERS:
                    mItemSupplierNameSpinner.setSelection(1);
                    break;
                case InventoryEntry.SUPPLIER_CULT_PENS:
                    mItemSupplierNameSpinner.setSelection(2);
                    break;
                case InventoryEntry.SUPPLIER_JET_PENS:
                    mItemSupplierNameSpinner.setSelection(3);
                    break;
                default:
                    mItemSupplierNameSpinner.setSelection(4);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemNameEditEditText.setText("");
        mItemPriceEditText.setText("");
        mTotalQuantityTextView.setText("Total Quantity: " + mQuantity + " / 100");
        mItemSupplierEmailEditText.setText("");
        mItemSupplierNameSpinner.setSelection(4);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory item.
                deleteInventoryItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of an inventory item in the database.
     */
    private void deleteInventoryItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentInventoryItemUri != null) {
            // Call the ContentResolver to delete the inventory item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryItemUri
            // content URI already identifies the inventory item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
