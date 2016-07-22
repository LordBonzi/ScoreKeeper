package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class RecyclerViewArrayAdapter extends DatabaseSelectableAdapter<RecyclerViewArrayAdapter.ViewHolder>{

    private ArrayList arrayList;
    private Context context;
    private DataHelper dataHelper = new DataHelper();
    private ViewHolder.ClickListener listener;
    private int activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewArrayAdapter(ArrayList titleArray, Context context1, ViewHolder.ClickListener listener, int activity) {
        context = context1;
        this.arrayList = titleArray;
        this.listener = listener;
        this.activity =  activity;

    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {

                    int count = 1;
                    while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                        ++count;
                    }

                    if (count == 1) {
                        removeItem(positions.get(0));
                    } else {
                        removeRange(positions.get(count - 1), count);
                    }

                    for (int i = 0; i < count; ++i) {
                        positions.remove(0);
                    }

            }
        }

    }

    public void deleteSelectedPresets(PresetDBAdapter presetDBAdapter, int gameID){
        if (activity == 1){
            for (int i = 0; i < getSelectedItems().size(); i++){
                int position = getSelectedItems().get(getSelectedItems().size() - i-1);
                presetDBAdapter.deletePreset(position);
            }

        }else if (activity == 2){
            Log.e("Arrayadapter", "f"+getSelectedItems());
            int oldSize = arrayList.size();
            for (int i = 0; i < getSelectedItems().size(); i++){
                if (arrayList.size() > 2) {
                    int position = getSelectedItems().get(i)-1 -(oldSize-arrayList.size());
                    arrayList.remove(position);
                    Log.e("Arrayadapter", "f"+arrayList);

                }
            }

            ScoreDBAdapter dbAdapter = new ScoreDBAdapter(context);
            dbAdapter.open().updateGame(arrayList, null, 0, ScoreDBAdapter.KEY_PLAYERS, gameID);
            dbAdapter.close();
        }
        notifyDataSetChanged();

    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            arrayList.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view

        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_array_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, listener);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(arrayList.get(position).toString());

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        if (isSelected(position+1)){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.stop));

        } else if (!isSelected(position+1)){

            holder.cardView.setCardBackgroundColor(outValue.resourceId);
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @SuppressWarnings("unused")

        // each data item is just a string in this case

        TextView textView;
        CardView cardView;
        ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

            this.listener = listener;
            textView = (TextView)v.findViewById(R.id.textView);
            cardView = (CardView) v.findViewById(R.id.cardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {

                listener.onItemClicked(getAdapterPosition(), getAdapterPosition()+1);
            }

        }


        public interface ClickListener {
            public void onItemClicked(int position, int gameID);
        }

    }




}
