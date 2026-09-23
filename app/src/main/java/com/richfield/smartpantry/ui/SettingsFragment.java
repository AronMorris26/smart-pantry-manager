package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.data.AppPreferences;
import com.richfield.smartpantry.data.AppPreferences.SortOrder;

/** Display preferences for the pantry list, stored in SharedPreferences. */
public class SettingsFragment extends Fragment {

    private AppPreferences preferences;
    private int[] windowDayValues;

    private MaterialSwitch highlightSwitch;
    private TextInputLayout windowLayout;
    private MaterialAutoCompleteTextView windowInput;
    private MaterialAutoCompleteTextView sortInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = new AppPreferences(requireContext());
        windowDayValues = getResources().getIntArray(R.array.expiry_window_days);

        highlightSwitch = view.findViewById(R.id.switch_highlight_expiring);
        windowLayout = view.findViewById(R.id.layout_expiry_window);
        windowInput = view.findViewById(R.id.input_expiry_window);
        sortInput = view.findViewById(R.id.input_sort_order);

        showCurrentValues();

        highlightSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            preferences.setHighlightExpiringEnabled(isChecked);
            windowLayout.setEnabled(isChecked);
        });

        windowInput.setOnItemClickListener((parent, itemView, position, id) ->
                preferences.setExpiryWindowDays(windowDayValues[position]));

        sortInput.setOnItemClickListener((parent, itemView, position, id) ->
                preferences.setSortOrder(position == 1 ? SortOrder.EXPIRY : SortOrder.NAME));
    }

    private void showCurrentValues() {
        boolean highlight = preferences.isHighlightExpiringEnabled();
        highlightSwitch.setChecked(highlight);
        windowLayout.setEnabled(highlight);

        String[] windowLabels = getResources().getStringArray(R.array.expiry_windows);
        windowInput.setText(windowLabels[indexOfWindowDays(preferences.getExpiryWindowDays())],
                false);

        String[] sortLabels = getResources().getStringArray(R.array.sort_orders);
        sortInput.setText(sortLabels[preferences.getSortOrder() == SortOrder.EXPIRY ? 1 : 0],
                false);
    }

    private int indexOfWindowDays(int days) {
        for (int i = 0; i < windowDayValues.length; i++) {
            if (windowDayValues[i] == days) {
                return i;
            }
        }
        return 0;
    }
}
