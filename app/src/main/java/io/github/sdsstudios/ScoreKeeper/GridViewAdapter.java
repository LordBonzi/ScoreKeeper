package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by seth on 16/07/16.
 */

public class GridViewAdapter extends BaseAdapter {
    private Context mCtx;
    private int mSelected;
    private final int[] mColors, mRawColors;
    private int mType;

    public GridViewAdapter(Context mCtx, int mSelected, int[] colors, int[] rawColors, int mType){
        this.mCtx = mCtx;
        this.mSelected = mSelected;
        this.mColors = colors;
        this.mRawColors = rawColors;
        this.mType = mType;
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mCtx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView = inflater.inflate(R.layout.accent_color_item, null);
        ImageView itemView = (ImageView) gridView.findViewById(R.id.accentColorView);

        itemView.setScaleType(ImageView.ScaleType.CENTER);

        gridView.setBackgroundColor(mRawColors[position]);

        if (mSelected == position) {
            itemView.setImageResource(R.mipmap.ic_check_white_24dp);

        }else{
            itemView.setBackgroundDrawable(null);

        }

        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mSelected = position;
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (mType == Themes.ACCENT_COLOR) {
                    editor.putInt("prefAccentColor", mColors[mSelected]);
                }else{
                    editor.putInt("prefPrimaryColor", mColors[mSelected]);
                    editor.putInt("prefPrimaryDarkColor", mRawColors[mSelected]);
                }

                editor.apply();
                notifyDataSetChanged();
            }
        });

        return gridView;
    }
}

