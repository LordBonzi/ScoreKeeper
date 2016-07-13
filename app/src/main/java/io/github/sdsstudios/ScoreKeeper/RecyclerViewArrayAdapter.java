package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class RecyclerViewArrayAdapter extends SelectableAdapter<RecyclerViewArrayAdapter.ViewHolder>{

    private ArrayList titleArray, itemsToDeleteList = new ArrayList();
    private Context context;
    private DataHelper dataHelper = new DataHelper();

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewArrayAdapter(ArrayList titleArray, Context context1) {
        context = context1;
        this.titleArray = titleArray;

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

    public void deleteSelectedGames(ScoreDBAdapter dbHelper){
        for (int i = 0; i < getSelectedItems().size(); i++){
            dbHelper.deleteGame(getSelectedItems().get(i));
        }

        notifyDataSetChanged();
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            titleArray.remove(positionStart);
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

        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    public List getItemsToDeleteList(){
        return itemsToDeleteList;
    }

    // Replace the contents of a view (invoked by the layout manager)
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(titleArray.get(position).toString());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                itemsToDeleteList.add(position + 1);
                Log.e("Recyclerviewadap", dataHelper.convertToString(itemsToDeleteList));
                holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.multiselect));

            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return titleArray.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder{
        @SuppressWarnings("unused")

        // each data item is just a string in this case

        TextView textView;
        LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);

            textView = (TextView)v.findViewById(R.id.textView);
            linearLayout = (LinearLayout)v.findViewById(R.id.linearLayout);
        }

    }




}
