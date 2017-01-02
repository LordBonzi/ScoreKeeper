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
    private String TAG = "SetViewAdapter";

    private List<Player> mPlayerArray;
    private int mNumPlayers;
    private Context mCtx;
    private Player mLastPlayer;
    private int mLastRow;
    private OnScoreClickListener mOnScoreClickListener;

    public SetGridViewAdapter(List<Player> mPlayerArray, Context ctx, OnScoreClickListener mOnScoreClickListener) {
        this.mPlayerArray = mPlayerArray;
        this.mCtx = ctx;
        this.mOnScoreClickListener = mOnScoreClickListener;

        mNumPlayers = mPlayerArray.size();
    }


    @Override
    public int getCount() {
        int num = mPlayerArray.size();
        for (Player p : mPlayerArray){
            num += p.getmSetScores().size();
        }
        return num;
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

                        if (mPlayerArray.indexOf(mLastPlayer) + 1 == mNumPlayers){
                            player = mPlayerArray.get(0);
                        }else{
                            player = mPlayerArray.get(mPlayerArray.indexOf(mLastPlayer) + 1);
                        }

                    }else{

                        player = mPlayerArray.get(1);

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
                    final List<Integer> setList = player.getmSetScores();

                    final int setPosition = setList.size() - currentRow - 1;

                    textView.setText(String.valueOf(setList.get(setPosition)));

                    final Player finalPlayer = player;
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnScoreClickListener.onScoreClick(finalPlayer, mPlayerArray.indexOf(finalPlayer), setPosition);
                        }
                    });

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

    public interface OnScoreClickListener {
        void onScoreClick(Player player, int position, int setPosition);
    }
}
