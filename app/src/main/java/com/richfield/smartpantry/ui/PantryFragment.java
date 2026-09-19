package com.richfield.smartpantry.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.richfield.smartpantry.AddEditIngredientActivity;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.data.PantryDao;
import com.richfield.smartpantry.model.PantryItem;

import java.util.List;

/**
 * Shows everything currently in the user's pantry, read from the database.
 *
 * <p>The list is reloaded in {@link #onResume()} rather than only when the fragment is created,
 * so it is already up to date when the user comes back from adding or editing an item.
 */
public class PantryFragment extends Fragment implements PantryAdapter.OnItemClickListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;

    private RecyclerView pantryList;
    private View emptyState;
    private FloatingActionButton addButton;

    /** Receives the result from the add/edit screen so the save can be confirmed to the user. */
    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registered here rather than later: the launcher has to exist before the fragment
        // reaches STARTED, or restoring a pending result after process death would crash.
        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    String savedName = result.getData()
                            .getStringExtra(AddEditIngredientActivity.EXTRA_SAVED_NAME);
                    if (savedName != null) {
                        showMessage(getString(R.string.item_saved, savedName));
                    }
                });
    }

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
        addButton = view.findViewById(R.id.fab_add_item);

        adapter = new PantryAdapter(this);
        pantryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        pantryList.setAdapter(adapter);

        addButton.setOnClickListener(button ->
                addEditLauncher.launch(AddEditIngredientActivity.createIntent(requireContext())));
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

    /** Anchored to the FAB so the message sits above it rather than covering it. */
    private void showMessage(@NonNull String message) {
        View root = getView();
        if (root == null) {
            return;
        }
        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAnchorView(addButton)
                .show();
    }

    /** Tapping a row opens the same screen in edit mode, identified by the item's id. */
    @Override
    public void onItemClick(@NonNull PantryItem item) {
        addEditLauncher.launch(
                AddEditIngredientActivity.createIntent(requireContext(), item.getId()));
    }
}
