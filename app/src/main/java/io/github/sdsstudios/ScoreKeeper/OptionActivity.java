package io.github.sdsstudios.ScoreKeeper;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.EditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static io.github.sdsstudios.ScoreKeeper.Activity.EDIT_GAME;

/**
 * Created by seth on 11/12/16.
 */

public abstract class OptionActivity extends ScoreKeeperActivity implements PlayerListAdapter.PlayerListAdapterListener {

    public static final String STATE_GAMEID = "mGameID";
    public static String TAG;

    public PlayerListAdapter mPlayerListAdapter;
    public RelativeLayout mRelativeLayout;
    public int mGameID;
    public RecyclerView mPlayerRecyclerView;
    public RecyclerView.LayoutManager mLayoutManager;
    private NestedScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();

        TAG = CURRENT_ACTIVITY == EDIT_GAME ? "EditGame" : "NewGame";

        Themes.themeActivity(this, CURRENT_ACTIVITY == EDIT_GAME ? R.layout.activity_edit_game : R.layout.activity_new_game
                , true);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);
        mPlayerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);

        mDbHelper.open();

        List<OptionCardView> mCardViewList = loadOptionCardViews();

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            AdView mAdView2 = (AdView) findViewById(R.id.adViewHome2);
            AdCreator adCreator2 = new AdCreator(mAdView2, this);
            adCreator2.createAd();
            mRelativeLayout = (RelativeLayout) findViewById(R.id.layoutEditGame);

            Bundle extras = getIntent().getExtras();

            mGameID = extras.getInt("GAME_ID");

            mGame = mDataHelper.getGame(mGameID, mDbHelper);
        } else {
            mRelativeLayout = (RelativeLayout) findViewById(R.id.newGameLayout);
        }

        for (final OptionCardView card : mCardViewList) {
            if (card.getmHeader().getId() != R.id.playersHeader) {
                card.getmHeader().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleCardViewHeight(card.getmHeight(), card, mScrollView.getBottom());

                    }
                });
            }

            card.getmContent().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int height = card.getmContent().getMeasuredHeight();

                    card.setmHeight(height);

                    if (card.getmHeader().getId() != R.id.playersHeader) {
                        toggleCardViewHeight(height, card, mScrollView.getScrollY());
                    }

                    removeOnGlobalLayoutListener(card.getmContent(), this);
                }
            });
        }

        if (savedInstanceState != null) {
            loadActivity(savedInstanceState);
        } else {
            loadActivity(null);
        }

        loadOptions();
    }

    public List<OptionCardView> loadOptionCardViews() {
        List<OptionCardView> mCardViewList = new ArrayList<>();
        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutOptions)
                , (RelativeLayout) findViewById(R.id.optionsHeader), 0));

        if (CURRENT_ACTIVITY == EDIT_GAME) {

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutDate)
                    , (RelativeLayout) findViewById(R.id.dateHeader), 0));

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutLength)
                    , (RelativeLayout) findViewById(R.id.lengthHeader), 0));

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutTitle)
                    , (RelativeLayout) findViewById(R.id.titleHeader), 0));

        } else {

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPresets)
                    , (RelativeLayout) findViewById(R.id.presetHeader), 0));

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutTimeLimit)
                    , (RelativeLayout) findViewById(R.id.timeLimitHeader), 0));

            mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPlayers)
                    , (RelativeLayout) findViewById(R.id.playersHeader), 0));
        }

        return mCardViewList;

    }

    public List<CheckBoxOption> CheckBoxOptions() {
        return mGame.getmCheckBoxOptions();
    }

    public List<IntEditTextOption> IntEditTextOptions() {
        return mGame.getmIntEditTextOptions();
    }

    public List<StringEditTextOption> StringEditTextOptions() {
        return mGame.getmStringEditTextOptions();
    }


    abstract void loadActivity(Bundle savedInstanceState);

    public void enableOptions(boolean enabled) {

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setEnabled(enabled);
        }

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setEnabled(enabled);
        }

        for (StringEditTextOption e : StringEditTextOptions()) {
            getEditText(e).setText(e.getString());
            getEditText(e).setEnabled(enabled);
        }
    }

    public void setOptionChangeListeners() {

        for (final CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    c.setData(b);
                    mDbHelper.open().updateGame(mGame);

                }
            });
        }

        for (final IntEditTextOption e : IntEditTextOptions()) {

            getEditText(e).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    try {

                        //if statement necessary to avoid numberformatexception if edittext empty
                        if (charSequence != "") {
                            e.setData(Integer.valueOf(charSequence.toString()));
                        } else {
                            e.setData(e.getmDefaultValue());
                        }

                    } catch (NumberFormatException error) {
                        e.setData(e.getmDefaultValue());

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {
                        mDbHelper.open().updateGame(mGame);

                }
            });
        }
    }

    public void invalidSnackbar(String message) {
        Snackbar snackbar;

        snackbar = Snackbar.make(mRelativeLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDbHelper.close();
    }

    private void toggleCardViewHeight(int height, OptionCardView cardView, int scrollTo) {
        if (cardView.getmHeader().getId() != R.id.playersHeader) {

            if (cardView.getmContent().getHeight() != height) {
                // expand

                expandView(height, cardView.getmContent(), scrollTo); //'height' is the height of screen which we have measured already.

            } else {
                // collapse
                collapseView(cardView);

            }
        }
    }

    public void collapseView(final OptionCardView cardView) {

        ValueAnimator anim = ValueAnimator.ofInt(cardView.getmHeight(), 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getmContent().getLayoutParams();
                layoutParams.height = val;
                cardView.getmContent().setLayoutParams(layoutParams);

            }
        });
        anim.start();
    }

    public void expandView(int height, final RelativeLayout layout, final int scrollTo) {

        ValueAnimator anim = ValueAnimator.ofInt(layout.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.height = val;
                layout.setLayoutParams(layoutParams);


            }
        });
        anim.start();

    }

    public CheckBox getCheckBox(CheckBoxOption checkBoxOption) {
        try {

            return ((CheckBox) findViewById(checkBoxOption.getmCheckBoxID()));

        } catch (ClassCastException e) {

            switch (checkBoxOption.getmID()) {
                case REVERSE_SCORING:
                    checkBoxOption.setmCheckBoxID(R.id.checkBoxReverseScoring);
                    return ((CheckBox) findViewById(R.id.checkBoxReverseScoring));

                case STOPWATCH:
                    checkBoxOption.setmCheckBoxID(R.id.checkBoxStopwatch);
                    return ((CheckBox) findViewById(R.id.checkBoxStopwatch));

                default:
                    return null;

            }

        }
    }

    public EditText getEditText(EditTextOption editTextOption) {
        try {

            return ((EditText) findViewById(editTextOption.getmEditTextID()));

        } catch (ClassCastException e) {

            switch (editTextOption.getmID()) {
                case NUMBER_SETS:
                    editTextOption.setmEditTextID(R.id.editTextNumSets);
                    return ((EditText) findViewById(R.id.editTextNumSets));

                case SCORE_DIFF_TO_WIN:
                    editTextOption.setmEditTextID(R.id.editTextDiffToWin);
                    return ((EditText) findViewById(R.id.editTextDiffToWin));

                case WINNING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextMaxScore);
                    return ((EditText) findViewById(R.id.editTextMaxScore));

                case STARTING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextStartingScore);
                    return ((EditText) findViewById(R.id.editTextStartingScore));

                case SCORE_INTERVAL:
                    editTextOption.setmEditTextID(R.id.editTextScoreInterval);
                    return ((EditText) findViewById(R.id.editTextScoreInterval));

                case LENGTH:
                    editTextOption.setmEditTextID(R.id.editTextLength);
                    return ((EditText) findViewById(R.id.editTextLength));

                case TITLE:
                    editTextOption.setmEditTextID(R.id.editTextTitle);
                    return ((EditText) findViewById(R.id.editTextTitle));

                case DATE:
                    editTextOption.setmEditTextID(R.id.editTextDate);
                    return ((EditText) findViewById(R.id.editTextDate));

                case DICE_MAX:
                    editTextOption.setmEditTextID(R.id.editTextDiceMax);
                    return ((EditText) findViewById(R.id.editTextDiceMax));

                case DICE_MIN:
                    editTextOption.setmEditTextID(R.id.editTextDiceMin);
                    return ((EditText) findViewById(R.id.editTextDiceMin));

                default:
                    return null;

            }
        }
    }

    public void displayRecyclerView(boolean editable) {
        mPlayerRecyclerView.setVisibility(View.VISIBLE);
        mLayoutManager = new LinearLayoutManager(this);
        mPlayerRecyclerView.setLayoutManager(mLayoutManager);
        mPlayerListAdapter = new PlayerListAdapter(CURRENT_ACTIVITY, editable, mRelativeLayout, this);
        mPlayerRecyclerView.setAdapter(mPlayerListAdapter);
    }

    public void setGameTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        mGame.setmTime(sdfDate.format(now));
    }

    public void deleteEmptyPlayers() {
        List<Player> playerArray = mGame.getmPlayerArray();

        for (int i = 0; i < playerArray.size(); i++) {
            if (playerArray.get(i).getmName().equals("")
                    || playerArray.get(i).getmName() == null
                    || playerArray.get(i).getmName().equals(" ")) {
                playerArray.remove(i);
            }
        }

        mGame.setmPlayerArray(playerArray);

    }

    public void loadOptions() {

        for (IntEditTextOption e : IntEditTextOptions()) {
            EditText editText = getEditText(e);

            if (e.getmDefaultValue() != e.getInt()) {
                editText.setText(String.valueOf(e.getInt()));
            } else {
                editText.setText("");
            }

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                editText.setEnabled(false);
            }
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            CheckBox checkBox = getCheckBox(c);

            checkBox.setChecked(c.isChecked());

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                checkBox.setEnabled(false);
            }
        }

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            for (EditTextOption e : StringEditTextOptions()) {
                getEditText(e).setHint(e.getString());
                getEditText(e).setEnabled(false);
            }
        }
    }

    /**
     * OnPlayerChangeListener
     **/
    @Override
    public void onPlayerChange(Player player, int position) {
        mGame.setPlayer(player, position);
        updateGame();

    }

    public void deleteGame() {
        mDbHelper.open().deleteGame(mGameID);
        mDbHelper.close();
    }

    @Override
    public void addPlayerToGame(Player player, int position) {

        mGame.addPlayerAtPosition(player, position);

        boolean areDuplicatePlayers = mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray());
        String playerName = player.getmName();

        if (areDuplicatePlayers) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(position);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();
        }

        if (playerName.equals("") || playerName.equals(" ")) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(position);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();

        } else if (!areDuplicatePlayers && !playerName.equals("") && !playerName.equals(" ")) {
            updateGame();
        }

    }

    @Override
    public List<Player> getPlayerArray() {
        return mGame.getmPlayerArray();
    }

    @Override
    public void onPlayerRemove(int position) {
        mGame.removePlayer(position);
    }
}
