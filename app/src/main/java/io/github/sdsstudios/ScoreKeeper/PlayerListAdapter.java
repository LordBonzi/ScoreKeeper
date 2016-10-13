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
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder>
        implements EditGame.PlayerListListener{

    public static int NEW_GAME = 1;
    public static int EDIT_GAME = 2;

    Snackbar snackbar = null;
    private Player backup;
    private Game mGame;
    public ScoreDBAdapter mDbHelper;
    private int mGameID;
    private int activity;
    private boolean editable;
    private RelativeLayout relativeLayout;
    private DataHelper dataHelper = new DataHelper();

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerListAdapter(Game game, ScoreDBAdapter dbHelper, int mActivity, boolean mEditable) {
        mGame = game;
        mDbHelper = dbHelper;
        mGameID = game.getmID();
        activity = mActivity;
        editable = mEditable;
        if(activity == NEW_GAME){
            relativeLayout = NewGame.relativeLayout;
        } else if (activity == EDIT_GAME) {
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

            holder.editTextPlayer.setText(mGame.getPlayer(position).getmName());
            holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.editTextPlayer.isEnabled()){
                        backup = null;
                        backup = new Player(holder.editTextPlayer.getText().toString(), 0, new ArrayList<Integer>());
                        mGame.setPlayerName(holder.editTextPlayer.getText().toString(), position);
                        if (dataHelper.checkPlayerDuplicates(mGame.getmPlayerArray())){
                            View.OnClickListener onClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            };

                            mGame.setPlayer(backup, position);

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
                            mDbHelper.updateGame(mGame);
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

            if (editable){
                holder.editTextPlayerExt.setEnabled(true);
                holder.editTextScoreExt.setEnabled(true);
                holder.buttonDelete.setVisibility(View.VISIBLE);
                holder.editTextPlayerExt.setText(mGame.getPlayer(position).getmName());
                holder.editTextScoreExt.setText(String.valueOf(mGame.getPlayer(position).getmScore()));
                holder.editTextPlayerExt.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            mGame.getPlayer(position).setmName(s.toString());
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
                            mGame.getPlayer(position).setmScore(Integer.valueOf(s.toString()));
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

            }else if(!editable){
                holder.editTextPlayerExt.setHint(mGame.getPlayer(position).getmName());
                holder.editTextScoreExt.setHint(String.valueOf(mGame.getPlayer(position).getmScore()));
                holder.editTextPlayerExt.setEnabled(false);
                holder.editTextScoreExt.setEnabled(false);
                holder.buttonDelete.setVisibility(View.INVISIBLE);

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mGame.size();
    }

    public void removeAt(int position) {
        backup = null;
        backup = mGame.getPlayer(position);
        mGame.removePlayer(position);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mGame.size());

        if (relativeLayout != null) {
            snackbar = Snackbar.make(relativeLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", onClickListener);
            snackbar.show();
        }

    }

    public void undoPlayerRemoval() {
        int playerArraySize = mGame.size();

        mGame.setPlayer( backup, playerArraySize);

        if (relativeLayout != null) {
            snackbar = Snackbar.make(relativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }else{
            snackbar = Snackbar.make(relativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        snackbar.show();

        notifyItemInserted(playerArraySize);
        notifyItemRangeChanged(playerArraySize, playerArraySize);

    }

    @Override
    public void addPlayer() {
        mGame.addPlayer(new Player("", 0, new ArrayList<Integer>()));
        notifyItemInserted(mGame.size());

    }

    @Override
    public Game getGame() {
        return mGame;
    }

    @Override
    public void deleteEmptyPlayers(Game game) {
        List<Player> playerArray = game.getmPlayerArray();
        for (int i = 0; i < playerArray.size(); i++){
            if (playerArray.get(i).getmName().equals("")||playerArray.get(i).getmName() ==null||playerArray.get(i).getmName().equals(" ")){
                playerArray.remove(i);
            }
        }

        game.setmPlayerArray(playerArray);

        this.mGame = game;
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
