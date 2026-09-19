package com.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.PantryItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Binds {@link PantryItem} objects to the rows of the pantry RecyclerView.
 *
 * <p>A RecyclerView only ever keeps enough row views on screen to fill it, and recycles them as
 * the user scrolls. {@link #onCreateViewHolder} inflates one of those reusable rows;
 * {@link #onBindViewHolder} is then called every time a row is reused to show a different item,
 * so it must set every field - leaving one unset would show the previous item's value.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Implemented by the fragment so the adapter itself need not know about navigation. */
    public interface OnItemClickListener {

        /** A row was tapped - open it for editing. */
        void onItemClick(@NonNull PantryItem item);

        /** A row was long-pressed - offer to remove it. */
        void onItemLongClick(@NonNull PantryItem item);
    }

    /** Only used on the main thread, so a shared formatter instance is safe here. */
    private static final SimpleDateFormat EXPIRY_FORMAT =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    /** Shows 2 as "2" and 1.5 as "1.5", rather than "2.0". */
    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("0.##");

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener clickListener;

    public PantryAdapter(@NonNull OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /** Replaces the whole list with a fresh read from the database. */
    public void setItems(@NonNull List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.name.setText(item.getName());
        holder.quantity.setText(
                QUANTITY_FORMAT.format(item.getQuantity()) + " " + item.getUnit());

        if (item.hasExpiryDate()) {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText(holder.itemView.getContext().getString(
                    R.string.expires_on, EXPIRY_FORMAT.format(new Date(item.getExpiryDate()))));
        } else {
            // Must be reset explicitly: this row may have just been recycled from an
            // item that did have an expiry date.
            holder.expiry.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> clickListener.onItemClick(item));

        holder.itemView.setOnLongClickListener(view -> {
            clickListener.onItemLongClick(item);
            // Returning true marks the long press as handled, so the row does not also
            // fire its normal click and open the edit screen behind the dialog.
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the row's views so findViewById is not called again on every bind. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView quantity;
        final TextView expiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
        }
    }
}
