package com.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Binds suggested recipes to the rows of the Suggested Recipes list. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(@NonNull Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener clickListener;

    public RecipeAdapter(@NonNull OnRecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setRecipes(@NonNull List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.name.setText(recipe.getName());
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.recipe_meta,
                recipe.getIngredients().size(),
                recipe.getServings(),
                recipe.getMinutes()));

        holder.itemView.setOnClickListener(view -> clickListener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView meta;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            meta = itemView.findViewById(R.id.recipe_meta);
        }
    }
}
