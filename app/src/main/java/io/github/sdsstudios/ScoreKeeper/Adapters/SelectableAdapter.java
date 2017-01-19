package io.github.sdsstudios.ScoreKeeper.Adapters;

/**
 * Created by seth on 30/06/16.
 */

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    @SuppressWarnings("unused")
    private static final String TAG = SelectableAdapter.class.getSimpleName();

    private List<Integer> mSelectedItems;

    public SelectableAdapter() {
        mSelectedItems = new ArrayList<>();
    }

    public boolean isSelected(int position) {

        return mSelectedItems.contains(position);

    }

    public void toggleSelection(int position) {

        if (mSelectedItems.contains(position)) {
            mSelectedItems.remove(mSelectedItems.indexOf(position));
        } else {
            mSelectedItems.add(position);
        }

        notifyItemChanged(position);
    }

    public void clearSelection() {
        List<Integer> selection = getmSelectedItems();
        mSelectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public List<Integer> getmSelectedItems() {
        return mSelectedItems;
    }

}
