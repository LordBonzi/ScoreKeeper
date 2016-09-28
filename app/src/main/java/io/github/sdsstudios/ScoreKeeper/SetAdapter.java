package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {

    private List<Player> mSetArray;
    private Context mContext;
    private DataHelper mDataHelper = new DataHelper();
    private int mNumPlayers, mNumSets;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SetAdapter(List<Player> setArray, Context context1, int numSets) {
        mContext = context1;
        this.mSetArray = setArray;
        this.mNumPlayers = setArray.size();
        this.mNumSets = numSets;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view

        View view = null;

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.set_adapter, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, mNumPlayers);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.e("setadapter", String.valueOf(mSetArray));
        Log.e("setadapter", String.valueOf(mNumSets));
        Log.e("setadapter", String.valueOf(position));

        if (mNumPlayers == 2){
        if (position == 0){
            holder.textViewP1.setText(mSetArray.get(position).toString());
            holder.textViewP2.setText(mSetArray.get(1).toString());
        }else {
            holder.textViewP1.setText(mSetArray.get(position * mNumPlayers).toString());
            holder.textViewP2.setText(mSetArray.get(position * mNumPlayers + 1).toString());
        }

        }else{

            String setString = "";
            if (position == 0){
                setString += mSetArray.get(0);
            }else{
                setString += mSetArray.get(position * mNumPlayers);
            }

            for (int i = 1; i < mNumPlayers; i++){
                    setString += ", " + mSetArray.get((mNumPlayers * position) + i);

            }

            holder.textView.setText(setString);
        }



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSetArray.size() / mNumPlayers;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder{
        @SuppressWarnings("unused")

        // each data item is just a string in this case
                //

        TextView textViewP1, textViewP2, textView;

        public ViewHolder(View v, int mNumPlayers) {
            super(v);

            if (mNumPlayers == 2){
                textViewP1 = (TextView) v.findViewById(R.id.textViewSet1);
                textViewP2 = (TextView) v.findViewById(R.id.textViewSet2);
                v.findViewById(R.id.relativeLayout2PlayerSet).setVisibility(View.VISIBLE);
                v.findViewById(R.id.relativeLayoutSet).setVisibility(View.INVISIBLE);

            }else{
                textView = (TextView) v.findViewById(R.id.textViewSets);
                v.findViewById(R.id.relativeLayoutSet).setVisibility(View.VISIBLE);
                v.findViewById(R.id.relativeLayout2PlayerSet).setVisibility(View.INVISIBLE);

            }
        }

    }
}
