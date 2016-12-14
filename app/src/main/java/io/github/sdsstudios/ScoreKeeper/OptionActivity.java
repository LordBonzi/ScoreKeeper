package io.github.sdsstudios.ScoreKeeper;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by seth on 11/12/16.
 */

public abstract class OptionActivity extends AppCompatActivity {

    public static final String STATE_GAMEID = "mGameID";
    public static int EDIT_GAME = 0;
    public static int NEW_GAME = 1;
    public static String TAG;
    public int CURRENT_ACTIVITY;
    public PlayerListAdapter mPlayerListAdapter;
    public Intent mHomeIntent;
    public RelativeLayout mRelativeLayout;
    public GameDBAdapter mDbHelper;
    public int mGameID;
    public Game mGame;
    public RecyclerView mPlayerRecyclerView;
    public DataHelper mDataHelper = new DataHelper();
    public SharedPreferences mSharedPreferences;
    public List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();
    public List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();
    public List<StringEditTextOption> mStringEditTextOptions = new ArrayList<>();
    public RecyclerView.LayoutManager mLayoutManager;
    private NestedScrollView mScrollView;

    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            removeLayoutListenerJB(v, victim);
        } else removeLayoutListener(v, victim);
    }

    @SuppressWarnings("deprecation")
    private static void removeLayoutListenerJB(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void removeLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();

        TAG = CURRENT_ACTIVITY == EDIT_GAME ? "EditGame" : "NewGame";

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Themes.themeActivity(this, CURRENT_ACTIVITY == EDIT_GAME ? R.layout.activity_edit_game : R.layout.activity_new_game
                , true);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), CURRENT_ACTIVITY);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getChildCount(); i++) {
            mTabLayout.getChildAt(i).setBackgroundColor(
                    mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this)));
        }

        mHomeIntent = new Intent(this, Home.class);

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);
        mPlayerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);

        mDbHelper = new GameDBAdapter(this);
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

                    if (card.getmHeader().getId() != R.id.playersHeader) {
                        int height = card.getmContent().getMeasuredHeight();
                        card.setmHeight(height);
                        toggleCardViewHeight(height, card, mScrollView.getScrollY());
                        removeOnGlobalLayoutListener(card.getmContent(), this);

                    }

                }
            });
        }

        if (savedInstanceState != null) {
            loadActivity(savedInstanceState);
        } else {
            loadActivity(null);
        }
    }

    public List<OptionCardView> loadOptionCardViews() {
        List<OptionCardView> mCardViewList = new ArrayList<>();
        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutOptions)
                , (RelativeLayout) findViewById(R.id.optionsHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPlayers)
                , (RelativeLayout) findViewById(R.id.playersHeader), 0));

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
        }

        return mCardViewList;

    }

    abstract int getActivity();

    abstract void loadActivity(Bundle savedInstanceState);

    public void enableOptions(boolean enabled) {

        for (CheckBoxOption c : mCheckBoxOptions) {
            getCheckBox(c).setEnabled(enabled);
        }

        for (IntEditTextOption e : mIntEditTextOptions) {
            getEditText(e).setEnabled(enabled);
        }

        for (StringEditTextOption e : mStringEditTextOptions) {
            getEditText(e).setText(e.getString());
            getEditText(e).setEnabled(enabled);
        }
    }

    public void setOptionChangeListeners() {

        for (final CheckBoxOption c : mCheckBoxOptions) {
            getCheckBox(c).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    c.setChecked(!c.isChecked());

                    if (CURRENT_ACTIVITY == NEW_GAME) {
                        mGame.setmCheckBoxOption(c);
                        mDbHelper.open().updateGame(mGame);
                    }

                }
            });

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                getCheckBox(c).setChecked(false);
            }
        }

        for (final IntEditTextOption e : mIntEditTextOptions) {

            getEditText(e).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    try {

                        //if statement necessary to avoid numberformatexception if edittext empty
                        if (charSequence != "") {
                            e.setInt(Integer.parseInt(charSequence.toString()));
                        } else {
                            e.setInt(e.getmDefaultValue());
                        }

                    } catch (NumberFormatException error) {
                        error.printStackTrace();
                        e.setInt(e.getmDefaultValue());

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if (CURRENT_ACTIVITY == NEW_GAME) {
                        mGame.setmIntEditTextOption(e);
                        mDbHelper.open().updateGame(mGame);
                    }

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
                case CheckBoxOption.REVERSE_SCORING:
                    checkBoxOption.setmCheckBoxID(R.id.checkBoxReverseScoring);
                    return ((CheckBox) findViewById(R.id.checkBoxReverseScoring));

                case CheckBoxOption.STOPWATCH:
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
                case IntEditTextOption.NUMBER_SETS:
                    editTextOption.setmEditTextID(R.id.editTextNumSets);
                    return ((EditText) findViewById(R.id.editTextNumSets));

                case EditTextOption.SCORE_DIFF_TO_WIN:
                    editTextOption.setmEditTextID(R.id.editTextDiffToWin);
                    return ((EditText) findViewById(R.id.editTextDiffToWin));

                case EditTextOption.WINNING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextMaxScore);
                    return ((EditText) findViewById(R.id.editTextMaxScore));

                case EditTextOption.STARTING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextStartingScore);
                    return ((EditText) findViewById(R.id.editTextStartingScore));

                case EditTextOption.SCORE_INTERVAL:
                    editTextOption.setmEditTextID(R.id.editTextScoreInterval);
                    return ((EditText) findViewById(R.id.editTextScoreInterval));

                case EditTextOption.LENGTH:
                    editTextOption.setmEditTextID(R.id.editTextLength);
                    return ((EditText) findViewById(R.id.editTextLength));

                case EditTextOption.TITLE:
                    editTextOption.setmEditTextID(R.id.editTextTitle);
                    return ((EditText) findViewById(R.id.editTextTitle));

                case EditTextOption.DATE:
                    editTextOption.setmEditTextID(R.id.editTextDate);
                    return ((EditText) findViewById(R.id.editTextDate));

                case EditTextOption.DICE_MAX:
                    editTextOption.setmEditTextID(R.id.editTextDiceMax);
                    return ((EditText) findViewById(R.id.editTextDiceMax));

                case EditTextOption.DICE_MIN:
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
        mPlayerListAdapter = new PlayerListAdapter(mGame, mDbHelper, CURRENT_ACTIVITY, editable, mRelativeLayout);
        mPlayerRecyclerView.setAdapter(mPlayerListAdapter);
    }

    public void setGameTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        mGame.setmTime(sdfDate.format(now));
    }

    public void updateGame() {
        mDbHelper.open().updateGame(mGame);
        mDbHelper.close();
    }

    public void loadOptions() {

        for (IntEditTextOption e : mGame.getmIntEditTextOptions()) {
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

        for (CheckBoxOption c : mGame.getmCheckBoxOptions()) {
            CheckBox checkBox = getCheckBox(c);

            checkBox.setChecked(c.isChecked());

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                checkBox.setEnabled(false);
            }
        }

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            for (EditTextOption e : mGame.getmStringEditTextOptions()) {
                getEditText(e).setHint(e.getString());
                getEditText(e).setEnabled(false);
            }
        }
    }

    public static class OptionActivityTabFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public OptionActivityTabFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static OptionActivityTabFragment newInstance(int sectionNumber) {
            OptionActivityTabFragment fragment = new OptionActivityTabFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = null;
            container.setVisibility(View.INVISIBLE);
            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        int mActivity;

        public SectionsPagerAdapter(FragmentManager fm, int activity) {
            super(fm);
            mActivity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a GameActivityTabFragment (defined as a static inner class below).
            return OptionActivityTabFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            if (mActivity == EDIT_GAME) {
                return 3;
            } else {
                return 2;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position == 2 && mActivity == EDIT_GAME) {
                return getString(R.string.sets);
            }

            switch (position) {
                case 0:
                    return mActivity == EDIT_GAME ? getString(R.string.info) : getString(R.string.options);
                case 1:
                    return mActivity == EDIT_GAME ? getString(R.string.options) : getString(R.string.advanced);

                default:
                    return getString(R.string.game);
            }
        }
    }
}
