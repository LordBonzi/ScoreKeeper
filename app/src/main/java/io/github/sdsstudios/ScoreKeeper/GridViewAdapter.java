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
    private Context context;
    private int selected;
    private final int[] colors;


    public GridViewAdapter(Context context, int selected, int[] colors){
        this.context = context;
        this.selected = selected;
        this.colors = colors;
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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView = inflater.inflate(R.layout.accent_color_item, null);
        ImageView itemView = (ImageView) gridView.findViewById(R.id.accentColorView);

        int[] rawColors = new int[] {
                context.getResources().getColor(R.color.accentGrey),
                context.getResources().getColor(R.color.accentPink),
                context.getResources().getColor(R.color.accentYellow),
                context.getResources().getColor(R.color.accentGreen),
                context.getResources().getColor(R.color.accentRed),
                context.getResources().getColor(R.color.accentPurple),
                context.getResources().getColor(R.color.accentOrange),
                context.getResources().getColor(R.color.accentBlue)

        };


        itemView.setScaleType(ImageView.ScaleType.CENTER);

        gridView.setBackgroundColor(rawColors[position]);
        if (selected == position +1) {
            itemView.setImageResource(R.mipmap.ic_check_white_24dp);

        }else{
            itemView.setBackgroundDrawable(null);

        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected = position+1;
                SharedPreferences sharedPreferences = context.getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("prefAccent", colors[selected-1]);

                editor.apply();
                notifyDataSetChanged();
            }
        });

        return gridView;
    }
}

