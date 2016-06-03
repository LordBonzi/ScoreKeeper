package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 08/05/16.
 */
public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    ScoreDBAdapter dbHelper;
    ArrayList arrayListScore;
    int gameID;
    private ArrayList<BigGameModel> mBigGameModel;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BigGameAdapter(ArrayList<BigGameModel> myDataset, ArrayList score, ScoreDBAdapter dbAdapter, int id) {
        mBigGameModel = myDataset;
        dbHelper =dbAdapter;
        arrayListScore = score;
        gameID = id;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BigGameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_game_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final BigGameModel bigGameModel = mBigGameModel.get(position);

        holder.textViewPlayer.setText(bigGameModel.getPlayers());
        holder.butonScore.setText(bigGameModel.getScore());
        holder.butonScore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int score = 0;
                int buttonScore = 0;

                buttonScore = Integer.valueOf(holder.butonScore.getText().toString());
                score = buttonScore += 1;
                holder.butonScore.setText(String.valueOf(score));
                arrayListScore.set(position, String.valueOf(score));
                dbHelper.updateGame(arrayListScore, null,ScoreDBAdapter.KEY_SCORE,gameID );

            }
        });

        holder.butonScore.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int score = 0;
                int buttonScore = 0;

                buttonScore = Integer.valueOf(holder.butonScore.getText().toString());
                score = buttonScore -= 1;

                if (score == -1){

                }else {
                    holder.butonScore.setText(String.valueOf(score));
                    arrayListScore.set(position, String.valueOf(score));
                    dbHelper.updateGame(arrayListScore, null, ScoreDBAdapter.KEY_SCORE,gameID );
                }

                return true;
            }
        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mBigGameModel.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public Button butonScore;


        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView)v.findViewById(R.id.listTextViewPlayer);
            butonScore = (Button) v.findViewById(R.id.listButtonScore);

        }
    }
}
