package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 20/09/16.
 */

public class SetGridViewAdapter extends BaseAdapter{
    private List<Player> mPlayerArray;
    private int mNumPlayers;
    private Context mCtx;
    private Player mLastPlayer;
    private int mLastRow;

    public SetGridViewAdapter(List<Player> mPlayerArray, Context ctx) {
        this.mPlayerArray = mPlayerArray;
        this.mCtx = ctx;

        mNumPlayers = mPlayerArray.size();
    }


    @Override
    public int getCount() {
        int num = mPlayerArray.size();
        for (Player p : mPlayerArray){
            num += p.getmSetScores().size();
        }
        return num + mNumPlayers;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public int getNumRows(){
        return (mPlayerArray.get(0).getmSetScores().size() * mNumPlayers) / mNumPlayers;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = null;

        if (view == null) {

            if (position < mPlayerArray.size()){
                itemView = inflater.inflate(R.layout.player_name_fragment, null);
                TextView textView = (TextView) itemView.findViewById(R.id.textView);
                textView.setTextSize(12);
                textView.setText(mPlayerArray.get(position).getmName());

            }else{

                Player player = null;
                int currentRow = 0;

                if (position == mNumPlayers){

                    player = mPlayerArray.get(0);
                    currentRow = 0;

                }else{

                    if (mNumPlayers - mPlayerArray.indexOf(mLastPlayer) < mNumPlayers){

                        player = mPlayerArray.get(mPlayerArray.indexOf(mLastPlayer) - 1);

                    }else{

                        player = mPlayerArray.get(mNumPlayers - 1);

                    }

                    if (position % mNumPlayers == 0) {

                        currentRow = mLastRow + 1;

                    }else{

                        currentRow = mLastRow;
                    }

                }

                mLastPlayer = player;
                mLastRow = currentRow;

                try {

                    if (currentRow == 0){
                        itemView = inflater.inflate(R.layout.player_name_fragment, null);

                    }else{
                        itemView = inflater.inflate(R.layout.set_fragment, null);

                    }

                    TextView textView = (TextView) itemView.findViewById(R.id.textView);

                    assert player != null;
                    textView.setText(String.valueOf(player.getmSetScores().get(currentRow)));

                } catch (Exception e) {

                    e.printStackTrace();
                    Log.e("SetAdapter", e.toString());
                    Log.e("SetAdapter", mLastPlayer.getmName());

                }

            }

        } else {
            itemView = view;
        }

        return itemView;
    }
}
