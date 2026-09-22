package com.richfield.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.richfield.smartpantry.data.RecipeDao;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.text.DecimalFormat;
import java.util.List;

/** Shows one recipe's full ingredient list and method. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.richfield.smartpantry.extra.RECIPE_ID";

    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("0.##");

    public static Intent createIntent(@NonNull Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationContentDescription(R.string.back);
        toolbar.setNavigationOnClickListener(view -> finish());

        applyWindowInsets();

        Recipe recipe = new RecipeDao(this)
                .findById(getIntent().getLongExtra(EXTRA_RECIPE_ID, Recipe.NO_ID));

        if (recipe == null) {
            finish();
            return;
        }

        setTitle(recipe.getName());
        showRecipe(recipe);
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.detail_root);
        View scroll = findViewById(R.id.detail_scroll);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, 0);
            scroll.setPadding(0, 0, 0, bars.bottom);
            return windowInsets;
        });
    }

    private void showRecipe(@NonNull Recipe recipe) {
        TextView meta = findViewById(R.id.recipe_meta);
        meta.setText(getString(R.string.recipe_meta,
                recipe.getIngredients().size(), recipe.getServings(), recipe.getMinutes()));

        LinearLayout ingredients = findViewById(R.id.ingredients_container);
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            addLine(ingredients,
                    QUANTITY_FORMAT.format(ingredient.getQuantity()) + " " + ingredient.getUnit(),
                    ingredient.getName());
        }

        LinearLayout steps = findViewById(R.id.steps_container);
        List<String> stepList = recipe.getStepList();
        for (int i = 0; i < stepList.size(); i++) {
            addLine(steps, String.valueOf(i + 1), stepList.get(i));
        }
    }

    private void addLine(@NonNull ViewGroup container,
                         @NonNull String lead,
                         @NonNull String body) {
        View line = LayoutInflater.from(this)
                .inflate(R.layout.item_detail_line, container, false);

        ((TextView) line.findViewById(R.id.line_lead)).setText(lead);
        ((TextView) line.findViewById(R.id.line_body)).setText(body);

        container.addView(line);
    }
}
