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
public class RecyclerViewArrayAdapter extends SelectableAdapter<RecyclerViewArrayAdapter.ViewHolder> {

    private String TAG = "RViewArrayAdapter";

    private List<String> mArrayList;
    private Context mCtx;
    private ViewHolder.ClickListener mClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewArrayAdapter(List<String> titleArray, Context context, ViewHolder.ClickListener listener) {
        mCtx = context;
        this.mArrayList = titleArray;
        this.mClickListener = listener;

        /** Remove create... and no timelimit items from array **/
        mArrayList.remove(0);
        mArrayList.remove(0);

    }

    public void deleteSelectedItems(Dialog type, Context context) {

        if (type == Dialog.PRESETS) {

            PresetDBAdapter presetDBAdapter = new PresetDBAdapter(context);

            for (int i = 0; i < getmSelectedItems().size(); i++) {
                int position = getmSelectedItems().get(i);
                presetDBAdapter.deletePreset(position);
            }

        }else{

            List<TimeLimit> timeLimitArray = TimeLimit.getTimeLimitArray(mCtx);

            for (int i = 0; i < getmSelectedItems().size(); i++){

                timeLimitArray.remove(getmSelectedItems().get(i) - i);

            }

            TimeLimit.saveTimeLimit(timeLimitArray, mCtx);

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
        holder.textView.setText(mArrayList.get(position));

        TypedValue outValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        if (isSelected(position)) {
            holder.cardView.setCardBackgroundColor(mCtx.getResources().getColor(R.color.stop));

        } else if (!isSelected(position)) {

            holder.cardView.setCardBackgroundColor(outValue.resourceId);
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @SuppressWarnings("unused")

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
                listener.onItemClicked(getAdapterPosition());
            }

        }

        public interface ClickListener {
            void onItemClicked(int position);
        }

    }
}
