package com.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.AlmostThereRecipe;
import com.richfield.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the suggestions screen's two sections to one scrolling list.
 *
 * <p>Two view types rather than two RecyclerViews, so the sections scroll together as one list
 * while staying visibly separate.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(@NonNull Recipe recipe);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    private final List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(@NonNull OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /** Rebuilds the list from the two sections, omitting any section that is empty. */
    public void setSections(@NonNull String readyTitle,
                            @NonNull List<Recipe> readyToCook,
                            @NonNull String almostTitle,
                            @NonNull List<AlmostThereRecipe> almostThere) {
        rows.clear();

        if (!readyToCook.isEmpty()) {
            rows.add(Row.header(readyTitle));
            for (Recipe recipe : readyToCook) {
                rows.add(Row.recipe(recipe, null));
            }
        }

        if (!almostThere.isEmpty()) {
            rows.add(Row.header(almostTitle));
            for (AlmostThereRecipe almost : almostThere) {
                rows.add(Row.recipe(almost.getRecipe(), almost.getMissingIngredient()));
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader() ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(
                    inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new RecipeViewHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText(row.header);
            return;
        }

        RecipeViewHolder recipeHolder = (RecipeViewHolder) holder;
        Recipe recipe = row.recipe;

        recipeHolder.name.setText(recipe.getName());
        recipeHolder.meta.setText(recipeHolder.itemView.getContext().getString(
                R.string.recipe_meta,
                recipe.getIngredients().size(),
                recipe.getServings(),
                recipe.getMinutes()));

        if (row.missingIngredient == null) {
            recipeHolder.missing.setVisibility(View.GONE);
        } else {
            recipeHolder.missing.setVisibility(View.VISIBLE);
            recipeHolder.missing.setText(recipeHolder.itemView.getContext()
                    .getString(R.string.recipe_missing, row.missingIngredient));
        }

        recipeHolder.itemView.setOnClickListener(view -> clickListener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static class Row {

        final String header;
        final Recipe recipe;
        final String missingIngredient;

        private Row(String header, Recipe recipe, String missingIngredient) {
            this.header = header;
            this.recipe = recipe;
            this.missingIngredient = missingIngredient;
        }

        static Row header(@NonNull String title) {
            return new Row(title, null, null);
        }

        static Row recipe(@NonNull Recipe recipe, @Nullable String missingIngredient) {
            return new Row(null, recipe, missingIngredient);
        }

        boolean isHeader() {
            return header != null;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView title;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.section_title);
        }
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView meta;
        final TextView missing;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            meta = itemView.findViewById(R.id.recipe_meta);
            missing = itemView.findViewById(R.id.recipe_missing);
        }
    }
}
