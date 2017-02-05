package io.github.sdsstudios.ScoreKeeper.Options;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Adapters.GameDBAdapter;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {

    /**
     * DO NOT CHANGE!!! WILL CRASH APP
     **/
    public static final String WINNING_SCORE = "winning_score";
    public static final String SCORE_INTERVAL = "score_interval";
    public static final String SCORE_DIFF_TO_WIN = "score_diff_to_win";
    public static final String NUMBER_SETS = "number_sets";
    public static final String STARTING_SCORE = "starting_score";
    public static final String DICE_MIN = "dice_min";
    public static final String DICE_MAX = "dice_max";
    public static final String LENGTH = "length";
    public static final String DATE = "date";
    public static final String TITLE = "title";
    public static final String REVERSE_SCORING = "reverse_scoring";
    public static final String STOPWATCH = "stopwatch";
    public static final String NOTES = "notes";

    private String mID;
    private boolean mBooleanData;
    private String mStringData;
    private Integer mIntData;

    private String mHint;

    public Option(String id, Object data, String mHint) {
        this.mID = id;
        this.mHint = mHint;

        setData(data);
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }

    public final String getmID() {
        return mID;
    }

    public int getInt() {
        return mIntData;
    }

    public void setData(Object mData) {
        if (mData instanceof Integer) {
            mIntData = (Integer) mData;

        } else if (mData instanceof String) {
            mStringData = (String) mData;

        } else if (mData instanceof Boolean) {

            mBooleanData = (Boolean) mData;
        }

    }

    public String getString(){
        return mStringData;
    }

    public boolean isChecked() {
        return mBooleanData;
    }


    public interface OptionListener {
        void onOptionChange(Option option, Activity activity, GameDBAdapter gameDBAdapter);
    }
}
