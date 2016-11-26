package io.github.sdsstudios.ScoreKeeper;

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

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends DatabaseSelectableAdapter<HistoryAdapter.ViewHolder> {
    private String TAG = "HistoryAdapter";

    static final int COMPLETED = 1;
    static final int UNFINISHED = 2;
    static final int BOTH = 3;

    private static List<HistoryModel> mItemArray;
    private Context mCtx;
    static boolean ACTION_MODE_DISABLED = true;
    private ViewHolder.ClickListener mViewClickListener;
    private int mActivity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<HistoryModel> mItemArray, Context context, ViewHolder.ClickListener clickListener, int activity) {
        this.mItemArray = mItemArray;
        this.mCtx = context;
        this.mViewClickListener = clickListener;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view

        View view;
        if (mActivity == Pointers.HOME){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recent_history_adapter, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_adapter, parent, false);
        }

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, mViewClickListener);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mItemArray.size() == 0){
            Toast.makeText(mCtx, "How did you start History? There are no games!!!. Email developer in About. Or leave a review and the developer will respond", Toast.LENGTH_LONG).show();

        }else {

            try {

                HistoryModel item = mItemArray.get(mItemArray.size() - position - 1);

                if (mActivity == Pointers.HISTORY){

                    TypedValue outValue = new TypedValue();

                    holder.textViewHistoryInfo.setText(item.getmInfo());

                    holder.textViewHistoryInProgress.setText(item.getmIsUnfinished());
                    holder.textViewHistoryInProgress.setAllCaps(true);

                    if (isSelected(item.getmID()) ) {
                        mCtx.getTheme().resolveAttribute(R.attr.multiSelectBackground, outValue, true);
                        holder.relativeLayout.setBackgroundResource(outValue.resourceId);
                    } else if (!isSelected(item.getmID()) || ACTION_MODE_DISABLED){
                        mCtx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                        holder.relativeLayout.setBackgroundResource(outValue.resourceId);
                    }
                }

                holder.textViewHistoryTitle.setText(item.getmTitle());
                holder.textViewHistoryPlayers.setText(item.getmPlayers());
                holder.textViewHistoryDate.setText(TimeHelper.gameDate(item.getmDate()));

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, e.toString());

            }

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItemArray.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @SuppressWarnings("unused")

        // each data item is just a string in this case
        public TextView textViewHistoryTitle;
        public TextView textViewHistoryPlayers;
        public TextView textViewHistoryDate;
        public TextView textViewHistoryInfo;
        public TextView textViewHistoryInProgress;
        public RelativeLayout relativeLayout;
        public int color;
        public String inProgress;
        public String completed;

        private ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

            textViewHistoryTitle = (TextView)v.findViewById(R.id.textViewHistoryPlayers);
            textViewHistoryDate = (TextView)v.findViewById(R.id.textViewHistoryDate);
            textViewHistoryPlayers = (TextView)v.findViewById(R.id.textViewHistoryScore);
            textViewHistoryInfo = (TextView)v.findViewById(R.id.textViewHistoryType);
            textViewHistoryInProgress = (TextView)v.findViewById(R.id.textViewHistoryInProgress);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.relativeLayoutHistoryAdapter);
            color = v.getResources().getColor(R.color.colorAccent);
            inProgress = v.getResources().getString(R.string.unfinished);

            this.listener = listener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

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
