package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Helper.TimeHelper;
import io.github.sdsstudios.ScoreKeeper.HistoryModel;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends DatabaseSelectableAdapter<HistoryAdapter.ViewHolder> {
    public static final int COMPLETED = 1;
    public static final int UNFINISHED = 2;
    public static final int BOTH = 3;
    public static boolean ACTION_MODE_DISABLED = true;
    private static List<HistoryModel> mItemArray;
    private String TAG = "HistoryAdapter";
    private Context mCtx;
    private ViewHolder.ClickListener mViewClickListener;
    private Activity mActivity;

    public HistoryAdapter(List<HistoryModel> mItemArray, Context context, ViewHolder.ClickListener clickListener, Activity activity) {
        HistoryAdapter.mItemArray = mItemArray;
        this.mCtx = context;
        this.mViewClickListener = clickListener;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view;
        if (mActivity == Activity.HOME) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recent_history_adapter, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_adapter, parent, false);
        }

        ViewHolder vh = new ViewHolder(view, mViewClickListener);

        return vh;
    }

    public void selectAllItems(){
        for (int i = 0; i < getItemCount(); i++){
            final HistoryModel item;
            item = mItemArray.get(mItemArray.size() - i - 1);
            toggleSelection(i , item.getmID());
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (mItemArray.size() == 0){
            Toast.makeText(mCtx, "How did you start History? There are no games!!!. Email developer in About. Or leave a review and the developer will respond", Toast.LENGTH_LONG).show();

        }else {

            try {

                HistoryModel item = mItemArray.get(mItemArray.size() - position - 1);

                if (mActivity == Activity.HISTORY) {

                    TypedValue outValue = new TypedValue();

                    holder.mTextViewHistoryInfo.setText(item.getmInfo());

                    holder.mTextViewHistoryInProgress.setText(item.getmIsUnfinished());
                    holder.mTextViewHistoryInProgress.setAllCaps(true);

                    if (isSelected(item.getmID()) ) {
                        mCtx.getTheme().resolveAttribute(R.attr.multiSelectBackground, outValue, true);
                        holder.mRelativeLayout.setBackgroundResource(outValue.resourceId);
                    } else if (!isSelected(item.getmID()) || ACTION_MODE_DISABLED){
                        mCtx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                        holder.mRelativeLayout.setBackgroundResource(outValue.resourceId);
                    }
                }

                holder.mTextViewHistoryTitle.setText(item.getmTitle());
                holder.mTextViewHistoryPlayers.setText(item.getmPlayers());
                holder.mTextViewHistoryDate.setText(TimeHelper.gameDate(item.getmDate()));

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, e.toString());

            }

        }

    }

    @Override
    public int getItemCount() {
        return mItemArray.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView mTextViewHistoryTitle;
        TextView mTextViewHistoryPlayers;
        TextView mTextViewHistoryDate;
        TextView mTextViewHistoryInfo;
        TextView mTextViewHistoryInProgress;
        RelativeLayout mRelativeLayout;

        private ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

            mTextViewHistoryTitle = (TextView) v.findViewById(R.id.textViewHistoryPlayers);
            mTextViewHistoryDate = (TextView) v.findViewById(R.id.textViewHistoryDate);
            mTextViewHistoryPlayers = (TextView) v.findViewById(R.id.textViewHistoryScore);
            mTextViewHistoryInfo = (TextView) v.findViewById(R.id.textViewHistoryType);
            mTextViewHistoryInProgress = (TextView) v.findViewById(R.id.textViewHistoryInProgress);
            mRelativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayoutHistoryAdapter);

            this.listener = listener;

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                final HistoryModel item;
                item = mItemArray.get(mItemArray.size()-getAdapterPosition()-1);
                listener.onItemClicked(getAdapterPosition(), item.getmID());
            }

        }

        @Override
        public boolean onLongClick(View view) {

            if (listener != null) {
                final HistoryModel item;
                item = mItemArray.get(mItemArray.size()-getAdapterPosition()-1);
                return listener.onItemLongClicked(getAdapterPosition(), item.getmID());
            }

            return true;
        }

        public interface ClickListener {
            void onItemClicked(int position, int gameID);
            boolean onItemLongClicked(int position, int gameID);
        }
    }




}
