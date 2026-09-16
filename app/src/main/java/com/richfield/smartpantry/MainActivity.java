package com.richfield.smartpantry;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.richfield.smartpantry.ui.PantryFragment;
import com.richfield.smartpantry.ui.SettingsFragment;
import com.richfield.smartpantry.ui.SuggestedRecipesFragment;

/**
 * Host activity for the three main screens.
 *
 * <p>Pantry, Suggested Recipes and Settings are fragments swapped into a single container by the
 * bottom navigation bar. The two screens reached from here - Add/Edit Ingredient and Recipe
 * Detail - are separate Activities started with explicit Intents, because they are tasks the
 * user finishes and returns from rather than tabs they switch between.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_nav);
        applyWindowInsets();

        bottomNav.setOnItemSelectedListener(item -> showFragmentFor(item.getItemId()));

        if (savedInstanceState == null) {
            // Opens on the pantry. Setting the id fires the listener above, which
            // performs the first fragment transaction.
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        } else {
            // The fragment and the selected tab are both restored for us, but the
            // toolbar title is not, so put it back.
            setTitle(titleFor(bottomNav.getSelectedItemId()));
        }
    }

    /**
     * Keeps content clear of the status bar and the gesture bar.
     *
     * <p>The app draws edge to edge, so the system does not reserve space for its bars. The top
     * inset is applied to the whole screen, but the bottom inset is applied as padding *inside*
     * the navigation bar rather than below it - that way the bar's background still extends to
     * the bottom of the screen instead of leaving a strip of empty window behind it.
     */
    private void applyWindowInsets() {
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, 0);
            bottomNav.setPadding(0, 0, 0, bars.bottom);
            return windowInsets;
        });
    }

    /** Swaps in the fragment for the given bottom-navigation item id. */
    private boolean showFragmentFor(int itemId) {
        Fragment fragment;
        if (itemId == R.id.nav_pantry) {
            fragment = new PantryFragment();
        } else if (itemId == R.id.nav_recipes) {
            fragment = new SuggestedRecipesFragment();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        setTitle(titleFor(itemId));
        return true;
    }

    @StringRes
    private int titleFor(int itemId) {
        if (itemId == R.id.nav_recipes) {
            return R.string.nav_recipes;
        }
        if (itemId == R.id.nav_settings) {
            return R.string.nav_settings;
        }
        return R.string.nav_pantry;
    }
}
