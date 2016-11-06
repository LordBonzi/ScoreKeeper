package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
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
    private boolean accent;

    public GridViewAdapter(Context mCtx, int mSelected, int[] colors, int[] rawColors, boolean accents){
        this.mCtx = mCtx;
        this.mSelected = mSelected;
        this.mColors = colors;
        this.mRawColors = rawColors;
        accent=accents;
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
        if (mSelected == position +1) {
            itemView.setImageResource(R.mipmap.ic_check_white_24dp);

        }else{
            itemView.setBackgroundDrawable(null);

        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelected = position+1;
                SharedPreferences sharedPreferences = mCtx.getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (accent) {
                    editor.putInt("prefAccent", mColors[mSelected - 1]);
                }else{
                    editor.putInt("prefPrimaryColor", mColors[mSelected - 1]);
                    editor.putInt("prefPrimaryDarkColor", mRawColors[mSelected - 1]);
                }

                editor.apply();
                notifyDataSetChanged();
            }
        });

        return gridView;
    }
}

