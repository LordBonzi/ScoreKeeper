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

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {

    private Snackbar mSnackBar = null;
    private Player mBackupPlayer;
    private List<Player> mPlayerList;
    private Activity mActivity;
    private boolean mEditable;
    private RelativeLayout mRelativeLayout;
    private DataHelper mDataHelper = new DataHelper();
    private PlayerChangeListener mPlayerChangeListener;

    public PlayerListAdapter(List<Player> mPlayerList, Activity mActivity, boolean mEditable, RelativeLayout mRelativeLayout
            , PlayerChangeListener playerChangeListener) {
        this.mActivity = mActivity;
        this.mEditable = mEditable;
        this.mRelativeLayout = mRelativeLayout;
        this.mPlayerList = mPlayerList;
        this.mPlayerChangeListener = playerChangeListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_list_adapter, parent, false);

        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (mActivity == Activity.NEW_GAME) {

            holder.layout.setVisibility(View.VISIBLE);
            holder.editTextPlayer.setText(mPlayerList.get(position).getmName());
            holder.buttonEdit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (holder.editTextPlayer.isEnabled()) {

                        Player player = mPlayerList.get(position);
                        mBackupPlayer = player;

                        player.setmName(holder.editTextPlayer.getText().toString());

                        if (mDataHelper.checkPlayerDuplicates(mPlayerList)) {
                            View.OnClickListener onClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mSnackBar.dismiss();
                                }
                            };

                            mPlayerList.set(position, mBackupPlayer);

                            if (mRelativeLayout != null) {
                                mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                        .setAction("Dismiss", onClickListener);
                                mSnackBar.show();
                            } else {
                                mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                        .setAction("Dismiss", onClickListener);
                                mSnackBar.show();
                            }

                        } else {

                            mPlayerChangeListener.onPlayerChange(player, position);

                            holder.editTextPlayer.setEnabled(false);
                            holder.buttonEdit.setImageResource(R.mipmap.ic_create_black_24dp);
                            holder.buttonDelete.setVisibility(View.VISIBLE);
                        }

                    } else {
                        holder.editTextPlayer.setEnabled(true);
                        holder.buttonEdit.setImageResource(R.mipmap.ic_check_white_24dp);
                        holder.buttonDelete.setVisibility(View.INVISIBLE);
                    }
                }
            });

        } else if (mActivity == Activity.EDIT_GAME) {
            holder.layoutExt.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.INVISIBLE);

            if (mEditable) {
                holder.editTextPlayerExt.setEnabled(true);
                holder.editTextScoreExt.setEnabled(true);
                holder.buttonDelete.setVisibility(View.VISIBLE);
                holder.editTextPlayerExt.setText(mPlayerList.get(position).getmName());
                holder.editTextScoreExt.setText(String.valueOf(mPlayerList.get(position).getmScore()));
                holder.editTextPlayerExt.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            Player player = mPlayerList.get(position);
                            player.setmName(s.toString());
                            mPlayerChangeListener.onPlayerChange(player, position);
                        } catch (Exception e) {
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
                        if (!s.toString().equals("")) {
                            Player player = mPlayerList.get(position);
                            player.setmScore(Integer.valueOf(s.toString()));
                            mPlayerChangeListener.onPlayerChange(player, position);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

            } else {

                holder.editTextPlayerExt.setHint(mPlayerList.get(position).getmName());
                holder.editTextScoreExt.setHint(String.valueOf(mPlayerList.get(position).getmScore()));
                holder.editTextPlayerExt.setEnabled(false);
                holder.editTextScoreExt.setEnabled(false);
                holder.buttonDelete.setVisibility(View.INVISIBLE);

            }

            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlayer(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPlayerList.size();
    }

    public void removePlayer(int position) {
        mBackupPlayer = mPlayerList.get(position);

        mPlayerChangeListener.onPlayerRemove(position);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        notifyItemRemoved(position);

        if (mRelativeLayout != null) {
            mSnackBar = Snackbar.make(mRelativeLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", onClickListener);
            mSnackBar.show();
        }

    }

    public void undoPlayerRemoval() {

        mPlayerChangeListener.addPlayerToGame(mBackupPlayer);

        if (mRelativeLayout != null) {
            mSnackBar = Snackbar.make(mRelativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            mSnackBar.show();
        } else {
            mSnackBar = Snackbar.make(mRelativeLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
            mSnackBar.show();
        }

        notifyItemInserted(mPlayerList.size());

    }

    public interface PlayerChangeListener {
        void onPlayerChange(Player player, int position);

        void addPlayerToGame(Player player);

        void onPlayerRemove(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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
            layout = (RelativeLayout) v.findViewById(R.id.relativeLayoutPlayerAdapter);
            layoutExt = (RelativeLayout) v.findViewById(R.id.playerAdapterExtended);
            check = v.getResources().getDrawable(R.mipmap.ic_check_white_24dp);
        }
    }
}
