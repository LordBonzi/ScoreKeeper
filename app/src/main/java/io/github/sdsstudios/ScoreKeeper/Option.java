package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {

    public static int NUM_STRING_OPTIONS = 3;

    private OptionID mID;
    private boolean mBooleanData;
    private String mStringData;
    private Integer mIntData;

    private String mHint;

    public Option(OptionID id, Object data, String mHint) {
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

    public OptionID getmID() {
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

    public enum OptionID {
        WINNING_SCORE, SCORE_INTERVAL, SCORE_DIFF_TO_WIN, NUMBER_SETS, STARTING_SCORE,
        DICE_MIN, DICE_MAX, LENGTH, DATE, TITLE, STOPWATCH, REVERSE_SCORING, PLAYER_LIST
    }

    public interface OptionListener {
        void onOptionChange(Option option, Activity activity, GameDBAdapter gameDBAdapter);
    }
}
