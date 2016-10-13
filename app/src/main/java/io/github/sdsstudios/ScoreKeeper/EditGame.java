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
    int gameID;
    private EditText editTextLength, editTextDate;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private DataHelper dataHelper;
    private PlayerListAdapter playerListAdapter;
    public static RelativeLayout editGameLayout;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat hourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    boolean bLength = false;
    private MenuItem menuItemDelete, menuItemEdit, menuItemDone, menuItemCancel, menuItemAdd
            , menuItemShare, menuItemComplete;
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
        gameID = extras.getInt("gameID");

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        dataHelper = new DataHelper();

        editTextDate = (EditText) findViewById(R.id.editTextDate);
        editTextLength = (EditText) findViewById(R.id.editTextLength);
        editGameLayout = (RelativeLayout) findViewById(R.id.edit_game_content);
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

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewEditGame);

        mGame = dataHelper.getGame(gameID, dbHelper);

        mEditTextOptions = EditTextOption.loadEditTextOptions(this);
        mCheckBoxOptions = CheckBoxOption.loadCheckBoxOptions(this);

        for (EditTextOption e : mEditTextOptions){

            if (e.getmData() != 0){

                e.getmEditText().setText(String.valueOf(e.getmData()));
            }

            e.getmEditText().setEnabled(false);
        }

        for (final CheckBoxOption c : mCheckBoxOptions){

            c.getmCheckBox().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (c.getmCheckBox().isChecked()){
                        c.setmChecked(true);
                    }else {
                        c.setmChecked(false);

                    }
                }
            });

            if (c.ismChecked()){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }

            c.getmCheckBox().setEnabled(false);

        }

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
                    // Remove the listener so it is not called repeatedly
                    removeOnGlobalLayoutListener(card.getmContent(), this);
                }
            });
        }

        editTextLength.setHint(mGame.getmLength());
        editTextDate.setHint(mGame.getmTime());
        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);

        displayRecyclerView(false);


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
            menuItemComplete.setTitle(R.string.complete);
            mGame.setmCompleted(true);
        }else{
            menuItemComplete.setTitle(R.string.unfinish);
            mGame.setmCompleted(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            getMenuInflater().inflate(R.menu.main, menu);
            mMenuItemList.add(menuItemDelete = menu.findItem(R.id.action_delete));
            mMenuItemList.add(menuItemDone = menu.findItem(R.id.action_done));
            mMenuItemList.add(menuItemEdit = menu.findItem(R.id.action_edit));
            mMenuItemList.add(menuItemCancel = menu.findItem(R.id.action_cancel));
            mMenuItemList.add(menuItemAdd = menu.findItem(R.id.action_add));
            mMenuItemList.add(menuItemComplete = menu.findItem(R.id.complete_game));
            mMenuItemList.add(menuItemShare = menu.findItem(R.id.menu_item_share).setVisible(true));

            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
            menuItemComplete.setVisible(true);

            updateCompleteMenuItem();

            createShareIntent();
            // Fetch and store ShareActionProvider
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemShare);
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
        dbHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    public void completeGame(){

        if (mGame.ismCompleted()){
            menuItemComplete.setTitle(R.string.complete);
        }else{
            menuItemComplete.setTitle(R.string.unfinish);
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
            playerListAdapter.addPlayer();

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
        editTextLength.setEnabled(true);
        editTextDate.setEnabled(true);

        menuItemAdd.setVisible(true);
        menuItemDelete.setVisible(false);
        menuItemEdit.setVisible(false);
        menuItemDone.setVisible(true);
        menuItemCancel.setVisible(true);
        menuItemShare.setVisible(false);
        menuItemComplete.setVisible(false);

        for (CheckBoxOption c: mCheckBoxOptions){
            c.getmCheckBox().setEnabled(true);
        }

        for (final EditTextOption e: mEditTextOptions){
            e.getmEditText().setEnabled(true);

            e.getmEditText().addTextChangedListener(new TextWatcher() {
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

        editTextLength.setText(mGame.getmLength());
        editTextDate.setText(mGame.getmTime());
        displayRecyclerView(true);

    }

    public void displayRecyclerView(boolean editable) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        playerListAdapter = new PlayerListAdapter(mGame, dbHelper, PlayerListAdapter.EDIT_GAME, editable);
        recyclerView.setAdapter(playerListAdapter);
    }

    public void onMenuDoneClick() {
        playerListAdapter.deleteEmptyPlayers(mGame);
        mGame = playerListAdapter.getGame();

        final String newDate = editTextDate.getText().toString();
        final String newLength = editTextLength.getText().toString();

        if (!checkValidity(newLength, hourlengthFormat, 10) && newLength.length() != 0){
            mGame.setOptionData(Option.STOPWATCH, true);
            bLength = true;
            invalidSnackbar(getString(R.string.invalid_time));

        }else if (newLength.length() == 0|| newLength.equals("")){
            bLength = false;
            mGame.setOptionData(Option.STOPWATCH, false);

        }else if(checkValidity(newLength, hourlengthFormat, 10) && newLength.length() != 0){
            bLength = false;
            mGame.setOptionData(Option.STOPWATCH, true);
        }

        final boolean bDateAndTime = checkValidity(editTextDate.getText().toString(), dateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = dataHelper.checkPlayerDuplicates(mGame.getmPlayerArray());
        final boolean bNumPlayers = moreThanTwoPlayers();

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                playerListAdapter.deleteEmptyPlayers(mGame);
                mGame = playerListAdapter.getGame();

                if (bCheckEmpty) {

                    invalidSnackbar("You can't have empty names!");

                }else if (!bDateAndTime) {
                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                } else if (bCheckDuplicates) {

                    invalidSnackbar("You can't have duplicate players!");

                } else if (bNumPlayers) {

                    invalidSnackbar("Must have 2 or more players");

                }else if (!bCheckEmpty && bDateAndTime && !bLength && !bCheckDuplicates && !bNumPlayers){
                    mGame.setmTime(newDate);
                    mGame.setmLength(newLength);

                    dbHelper.updateGame(mGame);

                    for (CheckBoxOption c : mCheckBoxOptions){
                        c.getmCheckBox().setEnabled(false);
                    }
                    for (EditTextOption e : mEditTextOptions){
                        e.getmEditText().setEnabled(false);
                    }

                    editTextLength.setText(mGame.getmLength());
                    editTextDate.setText(mGame.getmTime());

                    displayRecyclerView(false);

                    editTextLength.setEnabled(false);
                    editTextDate.setEnabled(false);
                    menuItemAdd.setVisible(false);
                    menuItemDelete.setVisible(true);
                    menuItemDone.setVisible(false);
                    menuItemEdit.setVisible(true);
                    menuItemCancel.setVisible(false);
                    menuItemShare.setVisible(true);
                    menuItemComplete.setVisible(true);
                    editTextDate.setEnabled(false);
                    editTextLength.setEnabled(false);

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    dbHelper.close();
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

        snackbar = Snackbar.make(editGameLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void onMenuCancelClick(){
        editTextLength.setText(mGame.getmLength());
        editTextDate.setText(mGame.getmTime());

        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);
        menuItemDelete.setVisible(true);
        menuItemDone.setVisible(false);
        menuItemEdit.setVisible(true);
        menuItemAdd.setVisible(false);
        menuItemCancel.setVisible(false);
        menuItemShare.setVisible(true);
        menuItemComplete.setVisible(true);
        editTextDate.setEnabled(false);
        editTextLength.setEnabled(false);

        for (CheckBoxOption c : mCheckBoxOptions){
            c.getmCheckBox().setEnabled(false);

            if (c.ismChecked()){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }
        }

        for (EditTextOption e :mEditTextOptions){
            e.getmEditText().setEnabled(false);

            e.getmEditText().setText("");
            if (e.getmData() != 0) {
                e.getmEditText().setText(String.valueOf(e.getmData()));
            }
        }

        displayRecyclerView(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public boolean moreThanTwoPlayers(){
        return mGame.size() >= 2;
    }

    public void delete(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_game);

        builder.setMessage(R.string.delete_game_message);

        builder.setPositiveButton(R.string.delete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.deleteGame(gameID);
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
