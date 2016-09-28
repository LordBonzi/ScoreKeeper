package io.github.sdsstudios.ScoreKeeper;

import android.widget.RelativeLayout;

/**
 * Created by Seth on 28/09/2016.
 */

public class OptionCardView {
    private RelativeLayout mContent;
    private RelativeLayout mHeader;
    private int mHeight;

    public OptionCardView(RelativeLayout mContent, RelativeLayout mHeader, int mHeight) {
        this.mContent = mContent;
        this.mHeader = mHeader;
        this.mHeight = mHeight;
    }

    public RelativeLayout getmContent() {
        return mContent;
    }

    public void setmContent(RelativeLayout mContent) {
        this.mContent = mContent;
    }

    public RelativeLayout getmHeader() {
        return mHeader;
    }

    public void setmHeader(RelativeLayout mHeader) {
        this.mHeader = mHeader;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }
}
