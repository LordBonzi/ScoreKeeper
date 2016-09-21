package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 20/09/16.
 */

public class SetGridViewAdapter extends BaseAdapter{
    private ArrayList mSetArray;
    private ArrayList mPlayerArray;
    private Context mCtx;

    public SetGridViewAdapter(ArrayList mSetArray, ArrayList mPlayerArray, Context ctx) {
        this.mSetArray = mSetArray;
        this.mPlayerArray = mPlayerArray;
        this.mCtx = ctx;
    }

    @Override
    public int getCount() {
        return mSetArray.size() + mPlayerArray.size();
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

        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView;

        if (view == null) {


            if (position < mPlayerArray.size()){
                itemView = inflater.inflate(R.layout.player_name_fragment, null);
                TextView textView = (TextView) itemView.findViewById(R.id.textView);
                textView.setText(String.valueOf(mPlayerArray.get(position)));

            }else{
                itemView = inflater.inflate(R.layout.set_fragment, null);
                TextView textView = (TextView) itemView.findViewById(R.id.textView);
                textView.setText(String.valueOf(mSetArray.get(position - mPlayerArray.size())));
            }


        } else {
            itemView = view;
        }

        return itemView;
    }
}
