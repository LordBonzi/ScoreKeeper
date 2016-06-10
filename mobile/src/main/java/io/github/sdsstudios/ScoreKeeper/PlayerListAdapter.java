package io.github.sdsstudios.ScoreKeeper;

import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder>{
    Snackbar snackbar = null;
    private String backup, backupScore;
    public static ArrayList<String> playerArray, scoreArray;
    public static ScoreDBAdapter mDbHelper;
    private int mGameID;
    private int activity;
    private int editable;


    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerListAdapter(ArrayList<String> player, ArrayList score, ScoreDBAdapter dbHelper, int gameID, int mactivity, int meditable) {
        playerArray = player;
        scoreArray = score;
        mDbHelper = dbHelper;
        mGameID = gameID;
        activity = mactivity;
        editable = meditable;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlayerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_list_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (activity == 1) {
            holder.layout.setVisibility(View.VISIBLE);
            holder.editTextPlayer.setText(playerArray.get(position));
            holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.editTextPlayer.isEnabled()){
                        playerArray.set(position, holder.editTextPlayer.getText().toString());
                        mDbHelper.updateGame(playerArray, null, ScoreDBAdapter.KEY_PLAYERS, mGameID);
                        holder.editTextPlayer.setEnabled(false);
                        holder.buttonEdit.setImageResource(R.mipmap.ic_create_black_24dp);
                        holder.buttonDelete.setVisibility(View.VISIBLE);
                    }else{
                        holder.editTextPlayer.setEnabled(true);
                        holder.buttonEdit.setImageResource(R.mipmap.ic_check_white_24dp);
                        holder.buttonDelete.setVisibility(View.INVISIBLE);
                    }
                }
            });

            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(position);
                }
            });

        } else if (activity == 2) {
            holder.layoutExt.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.INVISIBLE);

            if (editable == 1){
                holder.editTextPlayerExt.setEnabled(true);
                holder.editTextScoreExt.setEnabled(true);
                holder.buttonDelete.setVisibility(View.VISIBLE);
                holder.editTextPlayerExt.setText(playerArray.get(position));
                holder.editTextScoreExt.setText(scoreArray.get(position));
                holder.editTextPlayerExt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.i("ontextchanged", "" + playerArray);
                        playerArray.set(position, s.toString().trim());

                        if (!checkDuplicates(playerArray)){
                        }else {

                            Snackbar snackbar = Snackbar.make(EditGame.editGameLayout, "You can't have duplicate players!", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.editTextScoreExt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        scoreArray.set(position, s.toString().trim());

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(position);
                    }
                });
            }else if(editable == 0){
                holder.editTextPlayerExt.setHint(playerArray.get(position));
                holder.editTextScoreExt.setHint(scoreArray.get(position));

                holder.editTextPlayerExt.setEnabled(false);
                holder.editTextScoreExt.setEnabled(false);
                holder.buttonDelete.setVisibility(View.INVISIBLE);

            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return playerArray.size();
    }

    public void removeAt(int position) {
        backup = "";
        backup = "";
        Log.e("Player array removeat", "z " +playerArray);
        backup = playerArray.get(position);
        try {
            backupScore = scoreArray.get(position);
            scoreArray.remove(position);

        }catch (Exception e){

        }
        playerArray.remove(position);
        Log.e("Player array removed", "z " +playerArray);

        Snackbar snackbar = null;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, playerArray.size());

        if (activity == 2){
            snackbar = Snackbar.make(EditGame.editGameLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", onClickListener);
            snackbar.show();

        }else if (activity == 1){
            mDbHelper.updateGame(playerArray, null, ScoreDBAdapter.KEY_PLAYERS, mGameID);
            snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", onClickListener);
            snackbar.show();
        }


        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                backup = null;
                backupScore = null;

            }
        });

    }

    public void undoPlayerRemoval() {

        playerArray.add(playerArray.size(), backup);

        if (scoreArray != null) {
            scoreArray.add(scoreArray.size(), backupScore);
        }

        if (activity == 1){
            snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
        }else if (activity == 2){
            snackbar = Snackbar.make(EditGame.editGameLayout, "Undo complete.", Snackbar.LENGTH_SHORT);

        }
        snackbar.show();
        Log.e("Player array removed", "z " +playerArray);


        notifyItemRemoved(playerArray.size());
        notifyItemRangeChanged(playerArray.size(), playerArray.size());

    }


    public static void newPlayer(ScoreDBAdapter mDbHelper, int mGameID, PlayerListAdapter playerListAdapter){
        Log.e("Player array removed", "z " +playerArray);

        playerArray.add(playerArray.size(), "".trim());
        scoreArray.add(scoreArray.size(), "0");
        playerListAdapter.notifyItemInserted(playerArray.size());
        playerListAdapter.notifyItemRangeChanged(playerArray.size(), playerArray.size());

    }

    public static boolean checkDuplicates(ArrayList arrayList){
        boolean duplicate = false;

        Set<Integer> set = new HashSet<Integer>(arrayList);

        if(set.size() < arrayList.size()){
            duplicate = true;
        }

        return duplicate;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public EditText editTextPlayer, editTextPlayerExt, editTextScoreExt;
        public ImageButton buttonDelete;
        public ImageButton buttonEdit;
        public RelativeLayout layout, layoutExt;
        public Drawable check;

        public ViewHolder(View v) {
            super(v);
            editTextPlayer = (EditText) v.findViewById(R.id.editTextPlayer);
            editTextPlayerExt = (EditText) v.findViewById(R.id.editTextPlayerExt);
            editTextScoreExt = (EditText) v.findViewById(R.id.editTextScoreExt);
            buttonDelete = (ImageButton) v.findViewById(R.id.buttonDelete);
            buttonEdit = (ImageButton) v.findViewById(R.id.buttonEdit);
            layout = (RelativeLayout)v.findViewById(R.id.relativeLayoutPlayerAdapter);
            layoutExt = (RelativeLayout)v.findViewById(R.id.playerAdapterExtended);
            check = v.getResources().getDrawable(R.mipmap.ic_check_white_24dp);



        }

    }
}
