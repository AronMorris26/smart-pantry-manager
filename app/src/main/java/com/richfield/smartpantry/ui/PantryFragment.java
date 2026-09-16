package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;

/**
 * Shows everything currently in the user's pantry.
 *
 * <p>The list is populated from the database in a later step; for now the screen renders its
 * layout and empty state.
 */
public class PantryFragment extends Fragment {

    private RecyclerView pantryList;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryList = view.findViewById(R.id.pantry_list);
        emptyState = view.findViewById(R.id.empty_state);

        showEmptyState(true);
    }

    /** Swaps between the list and the "your pantry is empty" message. */
    private void showEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        pantryList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
