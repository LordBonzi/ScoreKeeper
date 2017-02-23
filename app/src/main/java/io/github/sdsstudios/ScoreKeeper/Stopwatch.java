package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth Schroeder on 08/06/2016.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Stopwatch extends TextView {
    @SuppressWarnings("unused")

    private static final String TAG = "Chronometer";
    private static final int TICK_WHAT = 2;
    private final int PAUSE_DELAY = 300;
    private long mBase;
    private boolean mVisible;
    private boolean mRunning;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StopwatchListener mStopwatchListener;
    private long timeElapsed;
    private boolean mPaused = false;
    private long mTimeWhenStopped = 0L;
    private String mTimeLimit;
    private boolean mStopwatchEnabledInGame;
    private boolean mGameOver;

    private int mStartColor, mStopColor, mTransparent, mDisabledColor;

    private Handler mOnPausedHandler = new Handler();

    private Runnable mOnPausedRunnable = new Runnable() {
        boolean red = false;

        @Override
        public void run() {
            if (red) {
                setTextColor(mTransparent);
            } else {
                setTextColor(mStopColor);
            }

            red = !red;
            mOnPausedHandler.postDelayed(this, PAUSE_DELAY);
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT),
                        100);
            }
        }
    };

    public Stopwatch(Context context) {
        this (context, null, 0);
    }

    public Stopwatch(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public Stopwatch(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);

        mDisabledColor = getTextColors().getDefaultColor();
        mStartColor = getResources().getColor(R.color.start);
        mTransparent = getResources().getColor(R.color.transparent);
        mStopColor = getResources().getColor(R.color.stop);
        mStopwatchListener = (StopwatchListener) context;
        setTextColor(mStartColor);

        init();
    }

    public void pause() {
        if (!mPaused && mStopwatchEnabledInGame) {
            toggle();
            mStopwatchListener.onStopwatchPause();
        }
    }

    public void setEnabled(boolean enabled) {
        mStopwatchEnabledInGame = enabled;
    }


    public void isGameOver(boolean gameOver) {
        /** pause() must be before setting mGameOver **/
        pause();

        mGameOver = gameOver;
        mOnPausedHandler.removeCallbacks(mOnPausedRunnable);
        setTextColor(mDisabledColor);
    }

    public long getTimeWhenStopped() {
        return mTimeWhenStopped;
    }

    public void setTimeLimit(String timeLimit) {
        this.mTimeLimit = timeLimit;
    }

    public void toggle() {
        if (mStopwatchEnabledInGame && !mGameOver) {
            mPaused = !mPaused;
            if (!mPaused) {
                setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);
                updateRunning();
                mOnPausedHandler.removeCallbacks(mOnPausedRunnable);
                setTextColor(mStartColor);
            } else {
                mOnPausedHandler.removeCallbacks(mOnPausedRunnable);
                mOnPausedHandler.postDelayed(mOnPausedRunnable, 0);
                mTimeWhenStopped = getBase() - SystemClock.elapsedRealtime();
                updateRunning();
                mStopwatchListener.onStopwatchPause();
            }
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
        updateText(mBase);
    }

    public long getBase() {
        return mBase;
    }

    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    public void setOnChronometerTickListener(
            OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    public void start() {
        if (mPaused && mStopwatchEnabledInGame) {
            toggle();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super .onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super .onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        timeElapsed = now - mBase;

        DecimalFormat df = new DecimalFormat("00");

        int hours = (int)(timeElapsed / (3600 * 1000));
        int remaining = (int)(timeElapsed % (3600 * 1000));

        int minutes = (int)(remaining / (60 * 1000));
        remaining = (int)(remaining % (60 * 1000));

        int seconds = (int)(remaining / 1000);
        remaining = (int)(remaining % (1000));

        int milliseconds = (int)(((int)timeElapsed % 1000) / 100);

        String text = "";

        text += df.format(hours) + ":";
        text += df.format(minutes) + ":";
        text += df.format(seconds) + ":";
        text += Integer.toString(milliseconds);
        setText(text);

    }

    private void updateRunning() {
        boolean running = mVisible && !mPaused;
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), 100);
            } else {
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    public void reset() {
        setBase(SystemClock.elapsedRealtime());
        mTimeWhenStopped = 0L;
    }

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
        isTimeLimitReached();
    }

    public boolean isTimeLimitReached() {

        if (mTimeLimit != null) {

            if (getText().equals(mTimeLimit)) {

                mStopwatchListener.onTimeLimitReached();

                return true;

            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public interface StopwatchListener {
        void onStopwatchPause();

        void onTimeLimitReached();
    }

    public interface OnChronometerTickListener {

        void onChronometerTick(Stopwatch chronometer);
    }

}
