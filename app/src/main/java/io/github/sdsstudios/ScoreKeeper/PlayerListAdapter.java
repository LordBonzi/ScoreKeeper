package io.github.sdsstudios.ScoreKeeper;

import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder>
        implements EditGame.PlayerListListener{
    Snackbar snackbar = null;
    private Player backup;
    private List<Player> mPlayerArray;
    public ScoreDBAdapter mDbHelper;
    private int mGameID;
    private int activity;
    private int editable;
    private RelativeLayout relativeLayout;
    private DataHelper dataHelper = new DataHelper();

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerListAdapter(List<Player> player, ScoreDBAdapter dbHelper, int gameID, int mactivity, int meditable) {
        mPlayerArray = player;
        mDbHelper = dbHelper;
        mGameID = gameID;
        activity = mactivity;
        editable = meditable;
        if(activity == 1){
            relativeLayout = NewGame.relativeLayout;
        } else if (activity == 2) {
            relativeLayout = EditGame.editGameLayout;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
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



            holder.editTextPlayer.setText(mPlayerArray.get(position).getmName());
            holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.editTextPlayer.isEnabled()){
                        backup = null;
                        backup = new Player(holder.editTextPlayer.getText().toString(), 0, new ArrayList<Integer>());
                        mPlayerArray.get(position).setmName(holder.editTextPlayer.getText().toString());
                        if (dataHelper.checkDuplicates(mPlayerArray)){
                            View.OnClickListener onClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            };

                            mPlayerArray.set(position, backup);

                            if (relativeLayout != null) {
                                snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                        .setAction("Dismiss", onClickListener);
                                snackbar.show();
                            }else{
                                snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                        .setAction("Dismiss", onClickListener);
                                snackbar.show();
                            }

                        }else{
                            mDbHelper.open();
                            mDbHelper.updatePlayers(mPlayerArray, mGameID);
                            mDbHelper.close();
                            holder.editTextPlayer.setEnabled(false);
                            holder.buttonEdit.setImageResource(R.mipmap.ic_create_black_24dp);
                            holder.buttonDelete.setVisibility(View.VISIBLE);
                        }

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
                holder.editTextPlayerExt.setText(mPlayerArray.get(position).getmName());
                holder.editTextScoreExt.setText(String.valueOf(mPlayerArray.get(position).getmScore()));
                holder.editTextPlayerExt.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            mPlayerArray.get(position).setmName(s.toString());
                        }catch (Exception e){
                            e.printStackTrace();
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
                        try {
                            mPlayerArray.get(position).setmScore(Integer.valueOf(s.toString()));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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
                holder.editTextPlayerExt.setHint(mPlayerArray.get(position).getmName());
                holder.editTextScoreExt.setHint(String.valueOf(mPlayerArray.get(position).getmScore()));
                holder.editTextPlayerExt.setEnabled(false);
                holder.editTextScoreExt.setEnabled(false);
                holder.buttonDelete.setVisibility(View.INVISIBLE);

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPlayerArray.size();
    }

    public void removeAt(int position) {
        backup = null;
        backup = mPlayerArray.get(position);
        mPlayerArray.remove(position);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mPlayerArray.size());

        if (relativeLayout != null) {
            snackbar = Snackbar.make(relativeLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", onClickListener);
            snackbar.show();
        }

    }

    public void undoPlayerRemoval() {

        mPlayerArray.add(mPlayerArray.size(), backup);

        if (relativeLayout != null) {
            snackbar = Snackbar.make(relativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }else{
            snackbar = Snackbar.make(relativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        snackbar.show();

        notifyItemInserted(mPlayerArray.size());
        notifyItemRangeChanged(mPlayerArray.size(), mPlayerArray.size());

    }

    @Override
    public void addPlayer() {
        mPlayerArray.add(mPlayerArray.size(), new Player("", 0, new ArrayList<Integer>()));
        notifyItemInserted(mPlayerArray.size());

    }

    @Override
    public List<Player> getPlayerArray() {
        return mPlayerArray;
    }

    @Override
    public void deleteEmptyPlayers(List<Player> playerArray) {
        for (int i = 0; i < playerArray.size(); i++){
            if (playerArray.get(i).getmName().equals("")||playerArray.get(i).getmName() ==null||playerArray.get(i).getmName().equals(" ")){
                playerArray.remove(i);
            }
        }
        this.mPlayerArray = playerArray;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder{
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
