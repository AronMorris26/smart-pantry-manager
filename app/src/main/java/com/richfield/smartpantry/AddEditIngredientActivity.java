package com.richfield.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.richfield.smartpantry.data.PantryDao;
import com.richfield.smartpantry.model.PantryItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Screen for adding a new ingredient or editing an existing one.
 *
 * <p>One screen serves both jobs. Which one it is depends entirely on whether the Intent that
 * started it carries {@link #EXTRA_ITEM_ID}: with an id the form is filled in from the database
 * and saving updates that row, without one the form starts blank and saving inserts a new row.
 * Callers build the Intent through the {@code createIntent} factories rather than assembling it
 * themselves, which keeps the extra keys private to this class.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra: id of the pantry item to edit. Absent means "add a new one". */
    public static final String EXTRA_ITEM_ID = "com.richfield.smartpantry.extra.ITEM_ID";

    /** Result extra: the name of the ingredient that was saved, for the confirmation message. */
    public static final String EXTRA_SAVED_NAME = "com.richfield.smartpantry.extra.SAVED_NAME";

    private static final String DATE_PICKER_TAG = "expiry_date_picker";
    private static final String STATE_EXPIRY_DATE = "state_expiry_date";

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    /** Shows 2 as "2" and 1.5 as "1.5", so an edited quantity does not read "2.0". */
    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("0.##");

    private PantryDao pantryDao;

    /** The item being edited, or null when adding a new one. */
    @Nullable
    private PantryItem editingItem;

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;

    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private AutoCompleteTextView unitInput;
    private TextInputEditText expiryInput;

    /** Currently chosen expiry date in epoch millis, or null when none is set. */
    @Nullable
    private Long selectedExpiryDate;

    /** Builds the Intent that opens this screen ready to add a new ingredient. */
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, AddEditIngredientActivity.class);
    }

    /** Builds the Intent that opens this screen to edit an existing ingredient. */
    public static Intent createIntent(@NonNull Context context, long itemId) {
        Intent intent = new Intent(context, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);

        pantryDao = new PantryDao(this);

        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        unitLayout = findViewById(R.id.layout_unit);

        nameInput = findViewById(R.id.input_name);
        quantityInput = findViewById(R.id.input_quantity);
        unitInput = findViewById(R.id.input_unit);
        expiryInput = findViewById(R.id.input_expiry);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        applyWindowInsets();

        clearErrorWhileTyping(nameLayout, nameInput);
        clearErrorWhileTyping(quantityLayout, quantityInput);
        clearErrorWhileTyping(unitLayout, unitInput);

        // The expiry field is never typed into - tapping it opens the calendar instead.
        expiryInput.setOnClickListener(view -> showDatePicker());
        findViewById(R.id.button_save).setOnClickListener(view -> save());

        loadItemBeingEdited();
        setTitle(editingItem == null
                ? R.string.title_add_ingredient
                : R.string.title_edit_ingredient);

        if (savedInstanceState == null) {
            if (editingItem != null) {
                populateForm(editingItem);
            }
        } else if (savedInstanceState.containsKey(STATE_EXPIRY_DATE)) {
            // The text fields restore themselves because they have ids, but the chosen date is
            // held in a field of this Activity, so it has to be restored by hand.
            setExpiryDate(savedInstanceState.getLong(STATE_EXPIRY_DATE));
        }

        reconnectDatePickerAfterRecreate();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedExpiryDate != null) {
            outState.putLong(STATE_EXPIRY_DATE, selectedExpiryDate);
        }
    }

    /** Reads the item id from the Intent, if this screen was opened to edit something. */
    private void loadItemBeingEdited() {
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NO_ID);
        if (itemId != PantryItem.NO_ID) {
            // Null if the item was deleted in the meantime, in which case this falls back
            // to behaving as an add screen rather than crashing.
            editingItem = pantryDao.findById(itemId);
        }
    }

    private void populateForm(@NonNull PantryItem item) {
        nameInput.setText(item.getName());
        quantityInput.setText(QUANTITY_FORMAT.format(item.getQuantity()));
        unitInput.setText(item.getUnit(), false);
        setExpiryDate(item.getExpiryDate());
    }

    /** Keeps the toolbar clear of the status bar and the Save button clear of the gesture bar. */
    private void applyWindowInsets() {
        View root = findViewById(R.id.add_edit_root);
        MaterialButton saveButton = findViewById(R.id.button_save);
        final int baseBottomMargin =
                ((ViewGroup.MarginLayoutParams) saveButton.getLayoutParams()).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, 0);

            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) saveButton.getLayoutParams();
            params.bottomMargin = baseBottomMargin + bars.bottom;
            saveButton.setLayoutParams(params);
            return windowInsets;
        });
    }

    /** Opens the calendar, starting on the date already chosen if there is one. */
    private void showDatePicker() {
        long startAt = selectedExpiryDate != null
                ? selectedExpiryDate
                : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_expiry)
                .setSelection(startAt)
                .build();

        picker.addOnPositiveButtonClickListener(this::setExpiryDate);
        picker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    /**
     * Re-attaches the listener to a calendar that was open when the screen was recreated.
     *
     * <p>The picker is a DialogFragment, so the system restores it across a rotation - but its
     * click listener was a lambda belonging to the previous Activity instance and is gone.
     * Without this, picking a date after rotating would silently do nothing.
     */
    @SuppressWarnings("unchecked")
    private void reconnectDatePickerAfterRecreate() {
        MaterialDatePicker<Long> restored = (MaterialDatePicker<Long>)
                getSupportFragmentManager().findFragmentByTag(DATE_PICKER_TAG);
        if (restored != null) {
            restored.addOnPositiveButtonClickListener(this::setExpiryDate);
        }
    }

    private void setExpiryDate(@Nullable Long millis) {
        selectedExpiryDate = millis;
        expiryInput.setText(millis == null ? "" : displayDateFormat.format(new Date(millis)));
    }

    /** Validates the form, writes the ingredient, and closes the screen. */
    private void save() {
        if (!isFormValid()) {
            return;
        }

        String name = textOf(nameInput);
        String unit = unitInput.getText().toString().trim();
        //noinspection ConstantConditions - isFormValid has already proved this parses.
        double quantity = parseQuantity(textOf(quantityInput));

        if (editingItem != null) {
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(selectedExpiryDate);
            pantryDao.update(editingItem);
        } else {
            PantryItem newItem = new PantryItem(name, quantity, unit, selectedExpiryDate);
            if (pantryDao.insert(newItem) == PantryDao.INSERT_FAILED) {
                return;
            }
        }

        finishWithResult(name);
    }

    /**
     * Checks every field and shows an inline error under each one that is wrong.
     *
     * <p>All three checks run even after one fails, so the user sees everything that needs
     * fixing at once rather than discovering the problems one save at a time.
     */
    private boolean isFormValid() {
        boolean nameValid = validateName();
        boolean quantityValid = validateQuantity();
        boolean unitValid = validateUnit();
        return nameValid && quantityValid && unitValid;
    }

    private boolean validateName() {
        String name = textOf(nameInput);
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            return false;
        }

        // An ingredient may appear only once, so adding one that is already there - or renaming
        // one onto another - would break the UNIQUE constraint on name_key. Comparing by id
        // lets an item keep its own name when edited.
        PantryItem clash = pantryDao.findByName(name);
        if (clash != null && (editingItem == null || clash.getId() != editingItem.getId())) {
            nameLayout.setError(getString(R.string.error_duplicate_name));
            return false;
        }

        nameLayout.setError(null);
        return true;
    }

    private boolean validateQuantity() {
        String raw = textOf(quantityInput);
        if (raw.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            return false;
        }

        Double quantity = parseQuantity(raw);
        if (quantity == null) {
            quantityLayout.setError(getString(R.string.error_quantity_invalid));
            return false;
        }
        if (quantity <= 0) {
            quantityLayout.setError(getString(R.string.error_quantity_positive));
            return false;
        }

        quantityLayout.setError(null);
        return true;
    }

    private boolean validateUnit() {
        if (unitInput.getText().toString().trim().isEmpty()) {
            unitLayout.setError(getString(R.string.error_unit_required));
            return false;
        }
        unitLayout.setError(null);
        return true;
    }

    /**
     * Parses a typed quantity, or returns null if it is not a number.
     *
     * <p>A comma is accepted as a decimal separator: the numeric keyboard offers whichever
     * separator the device locale uses, and Double.parseDouble only understands a full stop.
     */
    @Nullable
    private Double parseQuantity(@NonNull String raw) {
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException notANumber) {
            return null;
        }
    }

    /** Removes an error as soon as the user starts correcting the field it belongs to. */
    private void clearErrorWhileTyping(@NonNull TextInputLayout layout, @NonNull EditText field) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    /** Hands the saved name back to the pantry list so it can confirm what happened. */
    private void finishWithResult(@NonNull String savedName) {
        Intent result = new Intent();
        result.putExtra(EXTRA_SAVED_NAME, savedName);
        setResult(RESULT_OK, result);
        finish();
    }

    private String textOf(@NonNull TextInputEditText field) {
        CharSequence text = field.getText();
        return text == null ? "" : text.toString().trim();
    }
}
