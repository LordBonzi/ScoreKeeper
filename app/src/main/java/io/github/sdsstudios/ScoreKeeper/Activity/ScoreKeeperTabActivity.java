package io.github.sdsstudios.ScoreKeeper.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import io.github.sdsstudios.ScoreKeeper.Adapters.SetGridViewAdapter;
import io.github.sdsstudios.ScoreKeeper.Dialog;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;
import io.github.sdsstudios.ScoreKeeper.Tab.TabPager;

/**
 * Created by seth on 15/01/17.
 */

public abstract class ScoreKeeperTabActivity extends OptionActivity implements
        SetGridViewAdapter.OnScoreClickListener, ViewPager.OnPageChangeListener, ButtonPlayerListener {

    /**
     * Equal to the index of the tab
     **/
    public static final int SETS_LAYOUT = 1;
    public static final int GAME_LAYOUT = 0;
    public static final int OPTIONS_LAYOUT = 0;

    public GridView mSetGridView;
    public TabLayout mTabLayout;
    public SetGridViewAdapter mSetGridViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetGridView = (GridView) findViewById(R.id.setGridView);
        loadTabs();
    }

    public void populateSetGridView() {
        mSetGridView.setNumColumns(mGame.size());
        mSetGridViewAdapter = new SetGridViewAdapter(mGame.getmPlayerArray(), this, this);
        mSetGridView.setAdapter(mSetGridViewAdapter);
    }

    public void loadTabs() {
        TabPager mTabPager = new TabPager(getSupportFragmentManager(), this, CURRENT_ACTIVITY);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.option_tab_container);
        mViewPager.setAdapter(mTabPager);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getChildCount(); i++) {
            mTabLayout.getChildAt(i).setBackgroundColor(mPrimaryColor);
        }

        mViewPager.addOnPageChangeListener(this);
    }

    public abstract void chooseTab(int layout);

    @Override
    public void onScoreClick(Player player, int position, int setPosition) {
        playerDialog(player, position, Dialog.CHANGE_SET, setPosition);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

        switch (position) {

            case 0:
                chooseTab(GAME_LAYOUT);
                break;

            case 1:
                chooseTab(SETS_LAYOUT);
                populateSetGridView();
                break;

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void playerDialog(final Player player, final int position, final Dialog type, final int setPosition) {

        final Player oldPlayer = player;

        final int oldScore = (type == Dialog.CHANGE_SET)
                ? player.getmSetScores().get(setPosition)
                : player.getmScore();

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.edit_player_fragment, null);

        final EditText editTextPlayer = (EditText) dialogView.findViewById(R.id.editTextPlayer);
        final EditText editTextScore = (EditText) dialogView.findViewById(R.id.editTextScore);

        editTextPlayer.setHint(player.getmName());

        switch (type) {

            case CHANGE_SET:
                editTextScore.setHint(String.valueOf(player.getmSetScores().get(setPosition)));

                if (mGame.size() > 2) {
                    dialogBuilder.setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePlayer(position);
                        }
                    });
                }

                break;

            default:
                editTextScore.setHint(String.valueOf(player.getmScore()));
                break;

        }

        dialogBuilder.setPositiveButton(R.string.done, null);

        switch (type) {

            case EDIT_PLAYER:
                dialogBuilder.setTitle(getResources().getString(R.string.edit_player));
                break;

            case CHANGE_SET:
                dialogBuilder.setTitle(getResources().getString(R.string.change_set_score));
                break;

            case ADD_PLAYER:
                dialogBuilder.setTitle(getResources().getString(R.string.add_player));
                break;

        }

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type != Dialog.ADD_PLAYER) {
                    mGame.setPlayer(oldPlayer, position);
                }

                dialog.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);

        mAlertDialog = dialogBuilder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                editTextPlayer.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        player.setmName(editable.toString());
                    }
                });

                editTextScore.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (type == Dialog.CHANGE_SET) {
                            if (editable.toString().equals("")) {
                                player.changeSetScore(setPosition, oldScore);
                            } else {
                                player.changeSetScore(setPosition, Integer.valueOf(editable.toString()));
                            }
                        } else {
                            if (editable.toString().equals("")) {
                                player.setmScore(oldScore);
                            } else {
                                player.setmScore(Integer.valueOf(editable.toString()));
                            }
                        }

                    }
                });

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (type == Dialog.ADD_PLAYER) {
                            mGame.addNewPlayer(player);
                        } else {
                            mGame.setPlayer(player, position);
                        }

                        if (mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray())) {
                            if (type == Dialog.ADD_PLAYER) {
                                mGame.removePlayer(position);
                            } else {
                                mGame.setPlayer(oldPlayer, position);
                            }
                            Toast.makeText(getBaseContext(), R.string.duplicates_message, Toast.LENGTH_SHORT).show();

                        } else if (player.getmName().equals("")) {

                            if (type == Dialog.ADD_PLAYER) {
                                mGame.removePlayer(position);
                            } else {
                                mGame.setPlayer(oldPlayer, position);
                            }

                            Toast.makeText(getBaseContext(), R.string.must_have_name, Toast.LENGTH_SHORT).show();

                        } else {

                            mAlertDialog.dismiss();
                            forceUpdateGame();
                            mGame.isGameWon();
                            populateSetGridView();
                            goToCurrentSelectedTab();

                        }

                    }

                });
            }

        });
        mAlertDialog.show();

    }

    public void goToCurrentSelectedTab() {
        switch (mTabLayout.getSelectedTabPosition()) {
            case GAME_LAYOUT:
                chooseTab(GAME_LAYOUT);
                break;

            case SETS_LAYOUT:
                chooseTab(SETS_LAYOUT);
                break;
        }
    }

    @Override
    public void deletePlayer(int position) {

        if (mGame.size() > 2) {
            mGame.removePlayer(position);

        } else {
            Toast.makeText(this, R.string.more_than_two_players, Toast.LENGTH_SHORT).show();
        }

        updateGameInDatabase();

        populateSetGridView();
    }

    private void forceUpdateGame() {
        mDbHelper.open().updateGame(mGame);
        mDbHelper.close();
    }

    public void addPlayerDialog() {
        playerDialog(new Player("", mGame.getInt(Option.OptionID.STARTING_SCORE)), mGame.size(), Dialog.ADD_PLAYER, 0);

    }

}
