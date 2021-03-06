package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private static final int IMG_REQUEST = 88;
    private static final int PERMISSION_REQ = 11;
    private static final String LOG_TAG = "AddEditActivity";

    private ImageView mItemImageView;
    private Button mItemPicButton;
    private EditText mItemNameEditText;
    private EditText mItemPriceEditText;
    private Button mDecreaseButton;
    private EditText mItemQuantityEditText;
    private Button mIncreaseButton;
    private TextView mTotalQuantityTextView;
    private Spinner mItemSupplierNameSpinner;
    private EditText mItemSupplierEmailEditText;
    private Button mOrderMoreButton;

    private int mSupplier = 4;
    private String mCurrentItemSupplierName;
    private int mQuantity = 0;

    /**
     * Content URI for the existing inventory item (null if it's a new item)
     */
    private Uri mCurrentInventoryItemUri;
    private Uri mImageUri;
    private String mImageUriString = "";
    private boolean mImagePermission;

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

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ);
            }
        }

        mItemImageView = (ImageView) findViewById(R.id.item_image);
        mItemPicButton = (Button) findViewById(R.id.add_item_picture_button);
        mItemNameEditText = (EditText) findViewById(R.id.item_name_text);
        mItemPriceEditText = (EditText) findViewById(R.id.item_price_text);
        mDecreaseButton = (Button) findViewById(R.id.decrease_quantity);
        mItemQuantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        mIncreaseButton = (Button) findViewById(R.id.increase_quantity);
        mTotalQuantityTextView = (TextView) findViewById(R.id.total_quantity);
        mItemSupplierNameSpinner = (Spinner) findViewById(R.id.supplier_spinner);
        mItemSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email_text);
        mOrderMoreButton = (Button) findViewById(R.id.order_more_button);

        mTotalQuantityTextView.setText(getString(R.string.total_quantity, mQuantity));
        String quant = String.valueOf(mQuantity);
        mItemQuantityEditText.setText(quant);
        Intent inventoryIntent = getIntent();
        mCurrentInventoryItemUri = inventoryIntent.getData();

        if (mCurrentInventoryItemUri == null) {
            setTitle(getString(R.string.add_an_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_an_item));
            Log.v("AddEditActivity", "ID: " + mCurrentInventoryItemUri);
        }

        setupSpinner();
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        mItemNameEditText.setOnTouchListener(mTouchListener);
        mItemPriceEditText.setOnTouchListener(mTouchListener);
        mItemQuantityEditText.setOnTouchListener(mTouchListener);
        mItemSupplierNameSpinner.setOnTouchListener(mTouchListener);
        mItemSupplierEmailEditText.setOnTouchListener(mTouchListener);

        mItemPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderMore();
            }
        });

        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseButton();
            }
        });

        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseButton();
            }
        });
    }

    private void decreaseButton(){
        String currentQuantity = mItemQuantityEditText.getText().toString().trim();
        mQuantity = Integer.parseInt(currentQuantity);
        if(mQuantity > 0) {
            mQuantity--;
            mTotalQuantityTextView.setText(getString(R.string.total_quantity, mQuantity));
            String quant = String.valueOf(mQuantity);
            mItemQuantityEditText.setText(quant);
        } else {
            Toast.makeText(AddEditActivity.this, getString(R.string.decrease_toast), Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseButton(){
        String currentQuantity = mItemQuantityEditText.getText().toString().trim();
        mQuantity = Integer.parseInt(currentQuantity);
        if(mQuantity < 100) {
            mQuantity++;
            mTotalQuantityTextView.setText(getString(R.string.total_quantity, mQuantity));
            String quant = String.valueOf(mQuantity);
            mItemQuantityEditText.setText(quant);
        } else {
            Toast.makeText(AddEditActivity.this, getString(R.string.increase_toast), Toast.LENGTH_SHORT).show();
        }
    }

    public void getImage() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMG_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mImageUri = resultData.getData();
                mImageUriString = mImageUri.toString();
                mItemImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                Log.v(LOG_TAG, "Uri: " + mImageUriString);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mImagePermission = true;
                    Log.v(LOG_TAG, "Request : " + mImagePermission);

                } else {
                    mImagePermission = false;
                    Log.v(LOG_TAG, "Request : " + mImagePermission);
                    Toast.makeText(AddEditActivity.this, "App requires storage permission, please review your permissions", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mItemImageView.getWidth();
        int targetH = mItemImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Check validity of not null items
     */
    private boolean validation() {
        String name = mItemNameEditText.getText().toString().trim();
        String supplierEmail = mItemSupplierEmailEditText.getText().toString();
        Boolean existingImage = null;
        // If new item require image
        if (mCurrentInventoryItemUri == null) {
            if (mImageUri != null && name.length() != 0 && mQuantity >= 0 && mQuantity <= 100 && mSupplier >= 0 && mSupplier <= 4
                    && supplierEmail.length() != 0) {
                existingImage = true;
            } else {
                existingImage = false;
            }
        }

        // If existing item uploading a new image is optional as one already exists and the user might not want to change it
        if (mCurrentInventoryItemUri != null) {
            if (name.length() != 0 && mQuantity >= 0 && mQuantity <= 100 && mSupplier >= 0 && mSupplier <= 4
                    && supplierEmail.length() != 0) {
                existingImage = true;
            } else {
                existingImage = false;
            }
        }

            return existingImage;
        
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
                    if (selection.equals(getString(R.string.supplier_unknown))) {
                        mSupplier = InventoryEntry.SUPPLIER_UNKNOWN; // Supplier Unknown
                        mCurrentItemSupplierName = getString(R.string.supplier_unknown);
                    } else if (selection.equals(getString(R.string.supplier_one))) {
                        mSupplier = InventoryEntry.SUPPLIER_QUILL_LONDON; // Supplier Quill London
                        mCurrentItemSupplierName = getString(R.string.supplier_one);
                    } else if (selection.equals(getString(R.string.supplier_two))) {
                        mSupplier = InventoryEntry.SUPPLIER_SCRIBBLERS; // Supplier Scribblers
                        mCurrentItemSupplierName = getString(R.string.supplier_two);
                    } else if (selection.equals(getString(R.string.supplier_three))) {
                        mSupplier = InventoryEntry.SUPPLIER_CULT_PENS; // Supplier Cult Pens
                        mCurrentItemSupplierName = getString(R.string.supplier_three);
                    } else {
                        mSupplier = InventoryEntry.SUPPLIER_JET_PENS; // Supplier Jet Pens
                        mCurrentItemSupplierName = getString(R.string.supplier_four);
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

    private void orderMore() {
        String item = mItemNameEditText.getText().toString().trim();
        Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
        orderIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        orderIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_to, mCurrentItemSupplierName));
        orderIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_summary, item));
        if (orderIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(orderIntent);
        }
    }

    private void saveInventoryItem() {

        String itemName = mItemNameEditText.getText().toString().trim();
        String itemPrice = mItemPriceEditText.getText().toString().trim();
        String itemSupplierEmail = mItemSupplierEmailEditText.getText().toString().trim();

        if (mCurrentInventoryItemUri == null &&
                TextUtils.isEmpty(itemName) && TextUtils.isEmpty(itemPrice) &&
                TextUtils.isEmpty(itemSupplierEmail)
                && mSupplier == InventoryEntry.SUPPLIER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_IMAGE, mImageUriString);
        values.put(InventoryEntry.COLUMN_NAME, itemName);
        // If the price is not provided by the user, don't try to parse the string use 00.00 by default.
        if (TextUtils.isEmpty(itemPrice)) {
            String price = "00.00";
            values.put(InventoryEntry.COLUMN_PRICE, price);
        } else {
            values.put(InventoryEntry.COLUMN_PRICE, itemPrice);
        }
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
                if (validation()) {
                    saveInventoryItem();
                    finish();
                } else {
                    Toast.makeText(AddEditActivity.this, getString(R.string.save_validation), Toast.LENGTH_LONG).show();
                }
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
                InventoryEntry.COLUMN_IMAGE,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_EMAIL};

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
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String image = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplierName = cursor.getInt(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            if (image != null) {
                Uri imgUri = Uri.parse(image);
                mItemImageView.setImageURI(imgUri);
                mImageUriString = image;
            }
            Log.v(LOG_TAG, image);

            // Update the views on the screen with the values from the database
            mItemNameEditText.setText(name);
            mItemPriceEditText.setText(Double.toString(price));
            String quant = String.valueOf(quantity);
            mItemQuantityEditText.setText(quant);
            mTotalQuantityTextView.setText(getString(R.string.total_quantity, quantity));
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
        mItemNameEditText.setText("");
        mItemPriceEditText.setText("");
        String quant = String.valueOf(mQuantity);
        mItemQuantityEditText.setText(quant);
        mTotalQuantityTextView.setText(getString(R.string.total_quantity, mQuantity));
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
