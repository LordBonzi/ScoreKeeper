package io.github.sdsstudios.ScoreKeeper.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

    public GridView setGridView;
    public TabLayout tabLayout;
    public SetGridViewAdapter setGridViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setGridView = (GridView) findViewById(R.id.setGridView);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        loadTabs();
    }

    public void populateSetGridView() {
        setGridView.setNumColumns(game.size());
        setGridViewAdapter = new SetGridViewAdapter(game.getmPlayerArray(), this, this);
        setGridView.setAdapter(setGridViewAdapter);
    }

    public void loadTabs() {
        TabPager mTabPager = new TabPager(getSupportFragmentManager(), this, CURRENT_ACTIVITY);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.option_tab_container);
        mViewPager.setAdapter(mTabPager);

        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            tabLayout.getChildAt(i).setBackgroundColor(primaryColor);
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


        final AlertDialog.Builder dialogBuilder = createDialogBuilder(null, null);
        final View dialogView = layoutInflater.inflate(R.layout.edit_player_fragment, null);

        final EditText editTextPlayer = (EditText) dialogView.findViewById(R.id.editTextPlayer);
        final EditText editTextScore = (EditText) dialogView.findViewById(R.id.editTextScore);

        editTextPlayer.setHint(player.getmName());

        switch (type) {

            case CHANGE_SET:
                editTextScore.setHint(String.valueOf(player.getmSetScores().get(setPosition)));
                break;

            default:
                editTextScore.setHint(String.valueOf(player.getmScore()));
                break;

        }

        if (type != Dialog.ADD_PLAYER) {
            if (game.size() > 2) {
                dialogBuilder.setNeutralButton(getString(R.string.delete_player), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePlayer(position);
                    }
                });
            }
        }

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == Dialog.ADD_PLAYER) {
                    game.addNewPlayer(player);
                } else {
                    game.setPlayer(player, position);
                }

                if (dataHelper.checkPlayerDuplicates(game.getmPlayerArray())) {
                    if (type == Dialog.ADD_PLAYER) {
                        game.removePlayer(position);
                    } else {
                        game.setPlayer(oldPlayer, position);
                    }
                    Toast.makeText(getBaseContext(), R.string.duplicates_message, Toast.LENGTH_SHORT).show();

                } else if (player.getmName().equals("")) {

                    if (type == Dialog.ADD_PLAYER) {
                        game.removePlayer(position);
                    } else {
                        game.setPlayer(oldPlayer, position);
                    }

                    Toast.makeText(getBaseContext(), R.string.must_have_name, Toast.LENGTH_SHORT).show();

                } else {

                    forceUpdateGame();
                    game.isGameWon();
                    populateSetGridView();
                    goToCurrentSelectedTab();

                }
            }
        });

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
                    game.setPlayer(oldPlayer, position);
                }

                dialog.dismiss();

            }
        });

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

        dialogBuilder.setView(dialogView);

        dialogBuilder.create().show();
    }

    public void goToCurrentSelectedTab() {
        switch (tabLayout.getSelectedTabPosition()) {
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

        if (game.size() > 2) {
            game.removePlayer(position);

        } else {
            Toast.makeText(this, R.string.more_than_two_players, Toast.LENGTH_SHORT).show();
        }

        saveGameToDatabase();

        populateSetGridView();
    }

    private void forceUpdateGame() {
        gameDBAdapter.open().updateGame(game);
        gameDBAdapter.close();
    }

    public void addPlayerDialog() {
        playerDialog(new Player("", game.getInt(Option.STARTING_SCORE)), game.size(), Dialog.ADD_PLAYER, 0);

    }

}
