package com.richfield.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;

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
import com.richfield.smartpantry.data.PantryDao;
import com.richfield.smartpantry.model.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Screen for adding an ingredient to the pantry.
 *
 * <p>Started with an explicit Intent from {@link com.richfield.smartpantry.ui.PantryFragment}.
 * Callers build that Intent through {@link #createIntent(Context)} rather than constructing it
 * themselves, which keeps the extra keys private to this class.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String DATE_PICKER_TAG = "expiry_date_picker";

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private PantryDao pantryDao;

    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private AutoCompleteTextView unitInput;
    private TextInputEditText expiryInput;

    /** Currently chosen expiry date in epoch millis, or null when none is set. */
    private Long selectedExpiryDate;

    /** Builds the Intent that opens this screen ready to add a new ingredient. */
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, AddEditIngredientActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);

        pantryDao = new PantryDao(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_add_ingredient);
        toolbar.setNavigationOnClickListener(view -> finish());

        nameInput = findViewById(R.id.input_name);
        quantityInput = findViewById(R.id.input_quantity);
        unitInput = findViewById(R.id.input_unit);
        expiryInput = findViewById(R.id.input_expiry);

        applyWindowInsets();

        // The expiry field is not typed into - tapping it opens the calendar instead.
        expiryInput.setOnClickListener(view -> showDatePicker());
        findViewById(R.id.button_save).setOnClickListener(view -> save());
    }

    /** Keeps the toolbar clear of the status bar and the Save button clear of the gesture bar. */
    private void applyWindowInsets() {
        View root = findViewById(R.id.add_edit_root);
        MaterialButton saveButton = findViewById(R.id.button_save);
        final int baseBottomMargin = ((android.view.ViewGroup.MarginLayoutParams)
                saveButton.getLayoutParams()).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, 0);

            android.view.ViewGroup.MarginLayoutParams params =
                    (android.view.ViewGroup.MarginLayoutParams) saveButton.getLayoutParams();
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

    private void setExpiryDate(@Nullable Long millis) {
        selectedExpiryDate = millis;
        expiryInput.setText(millis == null ? "" : displayDateFormat.format(new Date(millis)));
    }

    /** Reads the form, writes the new ingredient, and closes the screen. */
    private void save() {
        String name = textOf(nameInput);
        String unit = unitInput.getText().toString().trim();
        double quantity = Double.parseDouble(textOf(quantityInput));

        PantryItem item = new PantryItem(name, quantity, unit, selectedExpiryDate);

        if (pantryDao.insert(item) == PantryDao.INSERT_FAILED) {
            return;
        }
        finish();
    }

    private String textOf(@NonNull TextInputEditText field) {
        CharSequence text = field.getText();
        return text == null ? "" : text.toString().trim();
    }
}
