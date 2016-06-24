package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 08/05/16.
 */
public class TimeLimitAdapter extends RecyclerView.Adapter<TimeLimitAdapter.ViewHolder> {
    private ArrayList timeLimitArray, timeLimitArrayNum;

    // Provide a suitable constructor (depends on the kind of dataset)
    public TimeLimitAdapter(ArrayList myDataset, ArrayList numArray) {
        timeLimitArray = myDataset;
        timeLimitArrayNum = numArray;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public TimeLimitAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.time_limit_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.textView.setText(timeLimitArray.get(position).toString());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return timeLimitArray.size() - 2;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView textView;
        public RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);

            relativeLayout = (RelativeLayout)v.findViewById(R.id.timeLimitAdapterLayout);
            textView = (TextView )v.findViewById(R.id.textViewTimeLimit);

        }
    }
}
