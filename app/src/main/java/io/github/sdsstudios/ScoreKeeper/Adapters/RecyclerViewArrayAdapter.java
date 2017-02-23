package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
    private Dialog mType;

    public RecyclerViewArrayAdapter(List<String> titleArray, Context context, ClickListener listener, Dialog type) {
        mCtx = context;
        this.mType = type;
        this.mArrayList = titleArray;
        this.mClickListener = listener;

        /** Remove "create..." and "no timelimit" items from array **/
        mArrayList.remove(0);

        if (type == Dialog.TIME_LIMIT) {
            mArrayList.remove(0);
        }

    }

    public void deleteSelectedItems() {

        if (mType == Dialog.PRESETS) {

            PresetDBAdapter presetDBAdapter = new PresetDBAdapter(mCtx);

            for (int i = 0; i < getmSelectedItems().size(); i++) {
                presetDBAdapter.deletePreset(getmSelectedItems().get(i) + 1);
            }

        }else{

            List<TimeLimit> timeLimitArray = TimeLimit.getTimeLimitArray(mCtx);

            for (int i = 0; i < getmSelectedItems().size(); i++){

                timeLimitArray.remove(getmSelectedItems().get(i) - i);

            }

            TimeLimit.saveTimeLimitArray(timeLimitArray, mCtx);

        }

        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_array_adapter, parent, false);

        ViewHolder vh = new ViewHolder(view, mClickListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mCheckBox.setText(mArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public interface ClickListener {
        void onItemClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        @SuppressWarnings("unused")

        private CheckBox mCheckBox;
        private ClickListener mListener;

        public ViewHolder(View v, ClickListener mListener) {
            super(v);

            this.mListener = mListener;
            mCheckBox = (CheckBox) v.findViewById(R.id.checkBox);
            mCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mListener.onItemClicked(getAdapterPosition());
        }
    }
}
