package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by seth on 30/06/16.
 */

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    @SuppressWarnings("unused")
    private static final String TAG = "DatabaseSelectableAdapter";

    private SparseIntArray selectedItems;

    public DatabaseSelectableAdapter() {
        selectedItems = new SparseIntArray();
    }

    /**
     * Indicates if the item at position position is selected
     * @param Position of the item to check
     * @return true if the item is selected, false otherwise
     */

    public boolean isSelected(int gameID) {

        return getSelectedItems().contains(gameID);

    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */

    public void toggleSelection(int position, int gameID) {

        if (getSelectedItems().contains(gameID)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, gameID);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<> (selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.valueAt(i));
        }
        return items;
    }

}
