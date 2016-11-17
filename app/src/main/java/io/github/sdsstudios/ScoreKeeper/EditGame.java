package io.github.sdsstudios.ScoreKeeper;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditGame extends AppCompatActivity {

    private String TAG = "EditGame";

    private int mGameID;
    private EditText mEditTextLength, mEditTextDate;
    private RecyclerView mRecyclerView;
    private ScoreDBAdapter mDbHelper;
    private DataHelper mDataHelper;
    private PlayerListAdapter mPlayerListAdapter;
    public static RelativeLayout EDIT_GAME_LAYOUT;
    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mHourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    private MenuItem mMenuItemDelete, mMenuItemEdit, mMenuItemDone, mMenuItemCancel, mMenuItemAdd
            , mMenuItemShare, mMenuItemComplete;
    private Intent mShareIntent;

    private NestedScrollView mScrollView;

    private List<OptionCardView> mCardViewList = new ArrayList<>();
    private List<MenuItem> mMenuItemList = new ArrayList<>();

    private List<EditTextOption> mEditTextOptions = new ArrayList<>();
    private List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

    private Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);

        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }

        setTheme(accentColor);
        setContentView(R.layout.activity_edit_game);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        AdView mAdView2 = (AdView) findViewById(R.id.adViewHome2);
        AdCreator adCreator2 = new AdCreator(mAdView2, this);
        adCreator2.createAd();
        getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        mGameID = extras.getInt("GAME_ID");

        mDbHelper = new ScoreDBAdapter(this);
        mDbHelper.open();

        mDataHelper = new DataHelper();

        mEditTextDate = (EditText) findViewById(R.id.editTextDate);
        mEditTextLength = (EditText) findViewById(R.id.editTextLength);
        EDIT_GAME_LAYOUT = (RelativeLayout) findViewById(R.id.edit_game_content);
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        ImageButton buttonHelpDate = (ImageButton) findViewById(R.id.buttonHelpDate);
        ImageButton buttonHelpLength = (ImageButton) findViewById(R.id.buttonHelpLength);
        buttonHelpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.date_and_time_help), getString(R.string.date_and_time_help_message));
            }
        });
        buttonHelpLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.length_help), getString(R.string.length_help_message));
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewEditGame);

        mGame = mDataHelper.getGame(mGameID, mDbHelper);

        mEditTextOptions = mGame.getmEditTextOptions();
        mCheckBoxOptions = mGame.getmCheckBoxOptions();

        loadOptions();

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutOptions)
                , (RelativeLayout) findViewById(R.id.optionsHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutDate)
                , (RelativeLayout) findViewById(R.id.dateHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPlayers)
                , (RelativeLayout) findViewById(R.id.playersHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutLength)
                , (RelativeLayout) findViewById(R.id.lengthHeader), 0));

        for (final OptionCardView card: mCardViewList){
            if (card.getmHeader().getId() == R.id.playersHeader && getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            }else{
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
                    // Do whatever you want with h
                    // Remove the onSharedPreferenceChangeListener so it is not called repeatedly
                    removeOnGlobalLayoutListener(card.getmContent(), this);
                }
            });
        }

        mEditTextLength.setHint(mGame.getmLength());
        mEditTextDate.setHint(mGame.getmTime());
        mEditTextLength.setEnabled(false);
        mEditTextDate.setEnabled(false);

        displayRecyclerView(false);


    }

    private CheckBox getCheckBox(int id){
        return ((CheckBox) findViewById(id));
    }

    private EditText getEditText(int id){
        return ((EditText) findViewById(id));
    }

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

    private void updateCompleteMenuItem(){
        if (mGame.ismCompleted()){
            mMenuItemComplete.setTitle(R.string.complete);
            mGame.setmCompleted(true);
        }else{
            mMenuItemComplete.setTitle(R.string.unfinish);
            mGame.setmCompleted(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            getMenuInflater().inflate(R.menu.main, menu);
            mMenuItemList.add(mMenuItemDelete = menu.findItem(R.id.action_delete));
            mMenuItemList.add(mMenuItemDone = menu.findItem(R.id.action_done));
            mMenuItemList.add(mMenuItemEdit = menu.findItem(R.id.action_edit));
            mMenuItemList.add(mMenuItemCancel = menu.findItem(R.id.action_cancel));
            mMenuItemList.add(mMenuItemAdd = menu.findItem(R.id.action_add));
            mMenuItemList.add(mMenuItemComplete = menu.findItem(R.id.complete_game));
            mMenuItemList.add(mMenuItemShare = menu.findItem(R.id.menu_item_share).setVisible(true));

            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
            mMenuItemComplete.setVisible(true);

            updateCompleteMenuItem();

            createShareIntent();
            // Fetch and store ShareActionProvider
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItemShare);
            mShareActionProvider.setShareIntent(mShareIntent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public void createShareIntent(){
        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, "");
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

    public void completeGame(){

        if (mGame.ismCompleted()){
            mMenuItemComplete.setTitle(R.string.complete);
        }else{
            mMenuItemComplete.setTitle(R.string.unfinish);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            delete();

        } else if (id == R.id.action_edit) {
            onMenuEditClick();

        } else if (id == R.id.action_done) {
            onMenuDoneClick();

        } else if (id == R.id.action_cancel) {
            onMenuCancelClick();

        } else if (id == R.id.action_add){
            mPlayerListAdapter.addPlayer();

        }else if (id == R.id.complete_game){
            completeGame();
        }

        return super.onOptionsItemSelected(item);
    }

    public void helpDialog(String title, String message){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void onMenuEditClick() {
        mEditTextLength.setEnabled(true);
        mEditTextDate.setEnabled(true);

        mMenuItemAdd.setVisible(true);
        mMenuItemDelete.setVisible(false);
        mMenuItemEdit.setVisible(false);
        mMenuItemDone.setVisible(true);
        mMenuItemCancel.setVisible(true);
        mMenuItemShare.setVisible(false);
        mMenuItemComplete.setVisible(false);

        for (CheckBoxOption c: mCheckBoxOptions){
            getCheckBox(c.getmCheckBoxID()).setEnabled(true);
        }

        for (final EditTextOption e: mEditTextOptions){
            EditText editText = getEditText(e.getmEditTextID());

            editText.setEnabled(true);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try
                    {
                        e.setmData(Integer.parseInt(charSequence.toString()));
                    }
                    catch (NumberFormatException error)
                    {
                        error.printStackTrace();
                        e.setmData(0);

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mEditTextLength.setText(mGame.getmLength());
        mEditTextDate.setText(mGame.getmTime());
        displayRecyclerView(true);

    }

    public void displayRecyclerView(boolean editable) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mPlayerListAdapter = new PlayerListAdapter(mGame, mDbHelper, Pointers.EDIT_GAME, editable);
        mRecyclerView.setAdapter(mPlayerListAdapter);
    }

    public void onMenuDoneClick() {
        mPlayerListAdapter.deleteEmptyPlayers(mGame);

        //TODO fix this mess with pulling Game from playerlistadapter

        mGame = mPlayerListAdapter.getGame();

        final String newDate = mEditTextDate.getText().toString();
        final String newLength = mEditTextLength.getText().toString();
        final boolean booleanLength;

        if (!checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            mGame.setChecked(CheckBoxOption.STOPWATCH, true);
            booleanLength = true;
            invalidSnackbar(getString(R.string.invalid_length));

        }else if (newLength.length() == 0|| newLength.equals("")){
            booleanLength = false;
            mGame.setChecked(CheckBoxOption.STOPWATCH, false);

        }else if(checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            booleanLength = false;
            mGame.setChecked(CheckBoxOption.STOPWATCH, true);
        }else{
            booleanLength = false;
        }

        final boolean bDateAndTime = checkValidity(mEditTextDate.getText().toString(), mDateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray());
        final boolean bNumPlayers = mGame.size() >= 2;

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mPlayerListAdapter.deleteEmptyPlayers(mGame);
                mGame = mPlayerListAdapter.getGame();

                if (bCheckEmpty) {

                    invalidSnackbar("You can't have empty names!");

                }else if (!bDateAndTime) {
                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                }else if (booleanLength) {
                    invalidSnackbar(getString(R.string.invalid_length));

                } else if (bCheckDuplicates) {

                    invalidSnackbar("You can't have duplicate players!");

                } else if (!bNumPlayers) {

                    invalidSnackbar("Must have 2 or more players");

                }else{
                    mGame.setmTime(newDate);
                    mGame.setmLength(newLength);

                    mDbHelper.updateGame(mGame);

                    for (CheckBoxOption c : mCheckBoxOptions){
                        getCheckBox(c.getmCheckBoxID()).setEnabled(false);
                    }
                    for (EditTextOption e : mEditTextOptions){
                        getEditText(e.getmEditTextID()).setEnabled(false);
                    }

                    mEditTextLength.setText(mGame.getmLength());
                    mEditTextDate.setText(mGame.getmTime());

                    displayRecyclerView(false);

                    mEditTextLength.setEnabled(false);
                    mEditTextDate.setEnabled(false);
                    mMenuItemAdd.setVisible(false);
                    mMenuItemDelete.setVisible(true);
                    mMenuItemDone.setVisible(false);
                    mMenuItemEdit.setVisible(true);
                    mMenuItemCancel.setVisible(false);
                    mMenuItemShare.setVisible(true);
                    mMenuItemComplete.setVisible(true);
                    mEditTextDate.setEnabled(false);
                    mEditTextLength.setEnabled(false);

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    mDbHelper.close();
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();

    }

    public boolean checkValidity(String string, SimpleDateFormat simpleDateFormat, int length) {
        boolean validity = false;

        try {
            Date dateDate = simpleDateFormat.parse(string);

            if(string.length() == length) {
                validity = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }

        return validity;
    }

    public void invalidSnackbar(String message) {
        Snackbar snackbar;

        snackbar = Snackbar.make(EDIT_GAME_LAYOUT, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void onMenuCancelClick(){
        mEditTextLength.setText(mGame.getmLength());
        mEditTextDate.setText(mGame.getmTime());

        mEditTextLength.setEnabled(false);
        mEditTextDate.setEnabled(false);
        mMenuItemDelete.setVisible(true);
        mMenuItemDone.setVisible(false);
        mMenuItemEdit.setVisible(true);
        mMenuItemAdd.setVisible(false);
        mMenuItemCancel.setVisible(false);
        mMenuItemShare.setVisible(true);
        mMenuItemComplete.setVisible(true);
        mEditTextDate.setEnabled(false);
        mEditTextLength.setEnabled(false);

        loadOptions();

        displayRecyclerView(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadOptions(){
        for (EditTextOption e : mEditTextOptions){

            if (e.getmData() != 0) {
                EditText editText = getEditText(e.getmEditTextID());
                editText.setText(String.valueOf(e.getmData()));
                editText.setEnabled(false);
            }

        }

        for(CheckBoxOption c : mCheckBoxOptions){
            CheckBox checkBox = getCheckBox(c.getmCheckBoxID());
            if (c.isChecked()){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }

            checkBox.setEnabled(false);
        }
    }

    public void delete(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_game);

        builder.setMessage(R.string.delete_game_message);

        builder.setPositiveButton(R.string.delete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDbHelper.deleteGame(mGameID);
                startActivity(new Intent(EditGame.this, History.class));
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public interface PlayerListListener{
        void addPlayer();
        Game getGame();
        void deleteEmptyPlayers(Game game);
    }
}
