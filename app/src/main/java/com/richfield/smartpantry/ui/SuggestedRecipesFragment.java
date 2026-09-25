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
import com.richfield.smartpantry.RecipeDetailActivity;
import com.richfield.smartpantry.data.PantryDao;
import com.richfield.smartpantry.data.RecipeDao;
import com.richfield.smartpantry.logic.RecipeMatcher;
import com.richfield.smartpantry.model.Recipe;


/** Lists only the recipes the pantry can currently cover, per the strict-matching rule. */
public class SuggestedRecipesFragment extends Fragment
        implements RecipeAdapter.OnRecipeClickListener {

    private RecipeDao recipeDao;
    private PantryDao pantryDao;
    private RecipeAdapter adapter;

    private RecyclerView recipeList;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipeDao = new RecipeDao(requireContext());
        pantryDao = new PantryDao(requireContext());

        recipeList = view.findViewById(R.id.recipe_list);
        emptyState = view.findViewById(R.id.empty_state);

        adapter = new RecipeAdapter(this);
        recipeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipeList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    /** Re-runs the match on every visit, so a pantry change is reflected immediately. */
    private void refreshSuggestions() {
        RecipeMatcher.Suggestions suggestions = RecipeMatcher.partition(
                recipeDao.getAll(), pantryDao.getAllByNameKey());

        adapter.setSections(
                getString(R.string.section_ready_to_cook), suggestions.getReadyToCook(),
                getString(R.string.section_almost_there), suggestions.getAlmostThere());

        // The empty state belongs to the strict list: showing only "almost there" recipes
        // without saying nothing matched would read as though they were suggestions.
        boolean nothingToCook = suggestions.getReadyToCook().isEmpty();
        emptyState.setVisibility(nothingToCook ? View.VISIBLE : View.GONE);
        recipeList.setVisibility(nothingToCook ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(@NonNull Recipe recipe) {
        startActivity(RecipeDetailActivity.createIntent(requireContext(), recipe.getId()));
    }
}
