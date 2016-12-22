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

    public Option(OptionID id, Object data) {
        this.mID = id;
        setData(data);
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
        DICE_MIN, DICE_MAX, LENGTH, DATE, TITLE, STOPWATCH, REVERSE_SCORING
    }

    public interface OptionListener {
        void onOptionChange(Option option, Activity activity, GameDBAdapter gameDBAdapter);
    }
}
