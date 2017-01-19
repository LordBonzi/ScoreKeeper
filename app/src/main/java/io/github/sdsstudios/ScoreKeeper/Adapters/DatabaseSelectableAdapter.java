package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 26/11/2016.
 */

public abstract class DatabaseSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private String TAG = "MyDBSelectableAdapter";
    private SparseIntArray mSelectedItems;

    public DatabaseSelectableAdapter() {
        mSelectedItems = new SparseIntArray();
    }

    public void toggleSelection(int position, int gameID) {

        if (getSelectedItems().contains(gameID)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, gameID);
        }

        notifyItemChanged(position);
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<> (mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.valueAt(i));
        }
        return items;
    }

    public boolean isSelected(int gameID) {
        return getSelectedItems().contains(gameID);
    }

    public void deleteSelectedGames(GameDBAdapter dbHelper) {

        dbHelper.deleteSelectedGames(getSelectedItems());

        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelectedItems.clear();
        for (Integer i : getSelectedItems()) {
            notifyItemChanged(i);
        }
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }


}
