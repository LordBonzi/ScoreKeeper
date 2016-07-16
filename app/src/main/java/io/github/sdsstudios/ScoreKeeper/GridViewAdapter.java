package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by seth on 16/07/16.
 */

public class GridViewAdapter extends BaseAdapter {
    private Context context;

    public GridViewAdapter(Context context){
        this.context = context;

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
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int[] colors = new int[] {
                context.getResources().getColor(R.color.accentGrey),
                context.getResources().getColor(R.color.accentPink),
                context.getResources().getColor(R.color.accentYellow),
                context.getResources().getColor(R.color.accentGreen),
                context.getResources().getColor(R.color.accentRed),
                context.getResources().getColor(R.color.accentPurple),
                context.getResources().getColor(R.color.accentOrange),
                context.getResources().getColor(R.color.accentBlue)

        };
        View gridView;

        gridView = inflater.inflate(R.layout.accent_color_item, null);
        gridView.setBackgroundColor(colors[position]);

        return gridView;
    }
}
