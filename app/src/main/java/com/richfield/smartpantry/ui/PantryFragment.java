package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.data.PantryDao;
import com.richfield.smartpantry.model.PantryItem;

import java.util.List;

/**
 * Shows everything currently in the user's pantry, read from the database.
 *
 * <p>The list is reloaded in {@link #onResume()} rather than only when the fragment is created,
 * so it is already up to date when the user comes back from adding, editing or deleting an item
 * on another screen.
 */
public class PantryFragment extends Fragment implements PantryAdapter.OnItemClickListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;

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

        pantryDao = new PantryDao(requireContext());

        pantryList = view.findViewById(R.id.pantry_list);
        emptyState = view.findViewById(R.id.empty_state);

        adapter = new PantryAdapter(this);
        pantryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        pantryList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPantry();
    }

    /**
     * Reloads the pantry and shows either the list or the empty state.
     *
     * <p>The read happens on the main thread. A pantry holds tens of rows rather than thousands,
     * so the query returns well inside a frame; moving it to a background thread would add
     * callback handling for no measurable gain at this size.
     */
    private void refreshPantry() {
        List<PantryItem> items = pantryDao.getAll();
        adapter.setItems(items);
        showEmptyState(items.isEmpty());
    }

    /** Swaps between the list and the "your pantry is empty" message. */
    private void showEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        pantryList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(@NonNull PantryItem item) {
        // Opens the Add/Edit Ingredient screen once that Activity exists.
    }
}
