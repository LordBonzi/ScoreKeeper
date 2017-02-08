package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Dialog;
import io.github.sdsstudios.ScoreKeeper.R;
import io.github.sdsstudios.ScoreKeeper.TimeLimit;

/**
 * Created by seth on 08/05/16.
 */
public class RecyclerViewArrayAdapter extends SelectableAdapter<RecyclerViewArrayAdapter.ViewHolder> {

    private String TAG = "RViewArrayAdapter";

    private List<String> mArrayList;
    private Context mCtx;
    private ClickListener mClickListener;

    public RecyclerViewArrayAdapter(List<String> titleArray, Context context, ClickListener listener) {
        mCtx = context;
        this.mArrayList = titleArray;
        this.mClickListener = listener;

        /** Remove "create..." and "no timelimit" items from array **/
        mArrayList.remove(0);
        mArrayList.remove(0);

    }

    public void deleteSelectedItems(Dialog type, Context context) {

        if (type == Dialog.PRESETS) {

            PresetDBAdapter presetDBAdapter = new PresetDBAdapter(context);

            for (int i = 0; i < getmSelectedItems().size(); i++) {
                int position = getmSelectedItems().get(i);
                presetDBAdapter.deletePreset(position);
            }

        }else{

            List<TimeLimit> timeLimitArray = TimeLimit.getTimeLimitArray(mCtx);

            for (int i = 0; i < getmSelectedItems().size(); i++){

                timeLimitArray.remove(getmSelectedItems().get(i) - i);

            }

            TimeLimit.saveTimeLimit(timeLimitArray, mCtx);

        }

        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view = null;

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_array_adapter, parent, false);

        ViewHolder vh = new ViewHolder(view, mClickListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(mArrayList.get(position));

        TypedValue outValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        if (isSelected(position)) {
            holder.mCardView.setCardBackgroundColor(mCtx.getResources().getColor(R.color.stop));

        } else if (!isSelected(position)) {

            holder.mCardView.setCardBackgroundColor(outValue.resourceId);
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public interface ClickListener {
        void onItemClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @SuppressWarnings("unused")

        private TextView mTextView;
        private CardView mCardView;
        private ClickListener mListener;

        public ViewHolder(View v, ClickListener mListener) {
            super(v);

            this.mListener = mListener;
            mTextView = (TextView) v.findViewById(R.id.textView);
            mCardView = (CardView) v.findViewById(R.id.cardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemClicked(getAdapterPosition());
            }

        }


    }
}
