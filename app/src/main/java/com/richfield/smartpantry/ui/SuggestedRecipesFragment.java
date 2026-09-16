package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.richfield.smartpantry.R;

/**
 * Lists the recipes the user can cook right now, according to the strict-matching rule.
 *
 * <p>Until the recipe collection and the matching logic exist, this correctly shows the
 * "no recipes match your pantry yet" empty state.
 */
public class SuggestedRecipesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }
}
