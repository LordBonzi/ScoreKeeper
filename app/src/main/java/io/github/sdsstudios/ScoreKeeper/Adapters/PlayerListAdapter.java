package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {

    private static String TAG = "PlayerListAdapter";
    private Snackbar mSnackBar = null;
    private View.OnClickListener dismissClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSnackBar.dismiss();
        }
    };
    private Player mBackupPlayer;
    private int mBackupPosition;
    private boolean mEditable;
    private RelativeLayout mRelativeLayout;
    private DataHelper mDataHelper = new DataHelper();
    private PlayerListAdapterListener mPlayerListAdapterListener;
    private View.OnClickListener undoPlayerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            undoPlayerRemoval();
        }
    };

    public PlayerListAdapter(boolean mEditable, RelativeLayout mRelativeLayout
            , PlayerListAdapterListener playerListAdapterListener) {
        this.mEditable = mEditable;
        this.mRelativeLayout = mRelativeLayout;
        this.mPlayerListAdapterListener = playerListAdapterListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_list_adapter, parent, false);

        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    private List<Player> mPlayerArray() {
        return mPlayerListAdapterListener.getPlayerArray();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mRelativeLayout.setVisibility(View.VISIBLE);
        holder.mEditTextPlayer.setText(mPlayerArray().get(position).getmName());
        holder.mButtonEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (holder.mEditTextPlayer.isEnabled()) {

                    Player player = mPlayerArray().get(position);
                    mBackupPlayer = player;

                    player.setmName(holder.mEditTextPlayer.getText().toString());

                    if (mDataHelper.checkPlayerDuplicates(mPlayerArray())) {

                        mPlayerArray().set(position, mBackupPlayer);

                        mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                .setAction("Dismiss", dismissClickListener);
                        mSnackBar.show();


                    } else {

                        mPlayerListAdapterListener.onPlayerChange(player, position);

                        holder.mEditTextPlayer.setEnabled(false);
                        holder.mButtonEdit.setImageResource(R.mipmap.ic_create_black_24dp);
                        holder.mButtonDelete.setVisibility(View.VISIBLE);
                    }

                } else {
                    holder.mEditTextPlayer.setEnabled(true);
                    holder.mButtonEdit.setImageResource(R.mipmap.ic_check_white_24dp);
                    holder.mButtonDelete.setVisibility(View.INVISIBLE);
                }
            }
        });

        holder.mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePlayer(position);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mPlayerArray().size();
    }

    public void removePlayer(int position) {

        mBackupPosition = position;
        mBackupPlayer = mPlayerArray().get(position);
        mPlayerListAdapterListener.onPlayerRemove(position);

        notifyDataSetChanged();

        if (mRelativeLayout != null) {
            mSnackBar = Snackbar.make(mRelativeLayout, "Player removed.", Snackbar.LENGTH_LONG)
                    .setAction("Undo", undoPlayerClickListener);
            mSnackBar.show();
        }


    }

    public void undoPlayerRemoval() {

        mPlayerListAdapterListener.addPlayerToGame(mBackupPlayer, mBackupPosition);
        notifyItemInserted(mBackupPosition);

    }

    public interface PlayerListAdapterListener {
        void onPlayerChange(Player player, int position);

        void addPlayerToGame(Player player, int position);

        void onPlayerRemove(int position);

        List<Player> getPlayerArray();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public EditText mEditTextPlayer;
        public ImageButton mButtonDelete;
        public ImageButton mButtonEdit;
        public RelativeLayout mRelativeLayout;
        public Drawable mDrawableChecked;

        public ViewHolder(View v) {
            super(v);
            mEditTextPlayer = (EditText) v.findViewById(R.id.editTextPlayer);
            mButtonDelete = (ImageButton) v.findViewById(R.id.buttonDelete);
            mButtonEdit = (ImageButton) v.findViewById(R.id.buttonEdit);
            mRelativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayoutPlayerAdapter);
            mDrawableChecked = v.getResources().getDrawable(R.mipmap.ic_check_white_24dp);
        }
    }
}
