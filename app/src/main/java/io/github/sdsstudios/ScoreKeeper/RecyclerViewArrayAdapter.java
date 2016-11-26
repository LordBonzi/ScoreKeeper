package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class RecyclerViewArrayAdapter extends DatabaseSelectableAdapter<RecyclerViewArrayAdapter.ViewHolder> {

    private List<String> mArrayList;
    private Context mCtx;
    private ViewHolder.ClickListener mClickListener;
    private int mActivity;


    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewArrayAdapter(List<String> titleArray, Context context1, ViewHolder.ClickListener listener, int activity) {
        mCtx = context1;
        this.mArrayList = titleArray;
        this.mClickListener = listener;
        this.mActivity = activity;

    }

    public void deleteSelectedPresets(PresetDBAdapter presetDBAdapter, int gameID) {
        if (mActivity == Pointers.NEW_GAME) {
            for (int i = 0; i < getSelectedItems().size(); i++) {
                int position = getSelectedItems().get(getSelectedItems().size() - i - 1);
                presetDBAdapter.deletePreset(position);
            }

        }
        notifyDataSetChanged();

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view


        View view = null;

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_array_adapter, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, mClickListener);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mArrayList.get(position).toString());

        TypedValue outValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        if (isSelected(position + 1)) {
            holder.cardView.setCardBackgroundColor(mCtx.getResources().getColor(R.color.stop));

        } else if (!isSelected(position + 1)) {

            holder.cardView.setCardBackgroundColor(outValue.resourceId);
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @SuppressWarnings("unused")

        // each data item is just a string in this case

                TextView textView;
        CardView cardView;
        ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

            this.listener = listener;
            textView = (TextView) v.findViewById(R.id.textView);
            cardView = (CardView) v.findViewById(R.id.cardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {

                listener.onItemClicked(getAdapterPosition(), getAdapterPosition() + 1);
            }

        }

        public interface ClickListener {
            void onItemClicked(int position, int gameID);
        }

    }
}
