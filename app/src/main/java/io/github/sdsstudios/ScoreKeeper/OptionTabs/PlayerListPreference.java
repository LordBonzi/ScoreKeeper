package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.Option;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.PlayerListAdapter;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 21/12/16.
 */

public class PlayerListPreference extends OptionPreference {

    private RecyclerView mPlayerRecyclerView;
    private EditText mEditTextPlayer;
    private Button mButtonAddPlayer;

    private List<Player> mPlayerList;
    private Activity mActivity;
    private PlayerListAdapter mPlayerListAdapter;
    private RelativeLayout mRelativeLayout;
    private PlayerListAdapter.PlayerChangeListener mPlayerChangeListener;

    public PlayerListPreference(Context context, List<Player> playerList, Option.OptionListener optionListener
            , Activity activity, RelativeLayout mRelativeLayout, PlayerListAdapter.PlayerChangeListener mPlayerChangeListener) {
        super(context, optionListener);

        if (activity == Activity.NEW_GAME) {
            setLayoutResource(R.layout.player_list_fragment);
        }

        this.mPlayerChangeListener = mPlayerChangeListener;
        this.mRelativeLayout = mRelativeLayout;
        this.mPlayerList = playerList;
        this.mActivity = activity;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mPlayerRecyclerView = (RecyclerView) holder.findViewById(R.id.playerRecyclerView);

        if (mActivity == Activity.NEW_GAME) {
            mEditTextPlayer = (EditText) holder.findViewById(R.id.editTextPlayer);

            mEditTextPlayer.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        addPlayer();
                        return true;
                    }
                    return false;
                }
            });

            mButtonAddPlayer = (Button) holder.findViewById(R.id.buttonAddPlayer);
            mButtonAddPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addPlayer();
                }
            });
        }

        populatePlayerRecyclerView(false);

    }

    private void addPlayer() {
        mPlayerListAdapter.addPlayer(mEditTextPlayer.getText().toString().trim());
        mPlayerListAdapter.notifyItemInserted(mPlayerList.size());
    }

    private void populatePlayerRecyclerView(boolean editable) {
        mPlayerRecyclerView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mPlayerRecyclerView.setLayoutManager(mLayoutManager);
        mPlayerListAdapter = new PlayerListAdapter(mPlayerList, mActivity, editable, mRelativeLayout, mPlayerChangeListener);
        mPlayerRecyclerView.setAdapter(mPlayerListAdapter);
    }

    @Override
    public Option.OptionID getID() {
        return Option.OptionID.PLAYER_LIST;
    }

    @Override
    public void setEnabled(boolean enabled) {
        populatePlayerRecyclerView(enabled);
    }

    @Override
    public void setOption(Option option) {

    }

}

