package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.widget.CheckBox;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Option;
import io.github.sdsstudios.ScoreKeeper.R;

import static io.github.sdsstudios.ScoreKeeper.Activity.EDIT_GAME;

/**
 * Created by seth on 21/12/16.
 */

public class CheckBoxPreference extends OptionPreference {

    private CheckBox mCheckBox;
    private CheckBoxOption mCheckBoxOption;
    private Activity mActivity;
    private GameDBAdapter mGameDBAdapter;

    public CheckBoxPreference(Context context, CheckBoxOption checkBoxOption, Option.OptionListener optionListener
            , Activity activity, GameDBAdapter gameDBAdapter) {
        super(context, optionListener);

        setLayoutResource(R.layout.check_box_option);

        this.mCheckBoxOption = checkBoxOption;
        this.mActivity = activity;
        this.mGameDBAdapter = gameDBAdapter;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mCheckBox = (CheckBox) holder.findViewById(R.id.checkBox);

        mCheckBox.setText(mCheckBoxOption.getmHint());
        mCheckBox.setChecked(mCheckBoxOption.isChecked());
        mCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mCheckBoxOption.setData(!mCheckBox.isChecked());

                mOptionListener.onOptionChange(mCheckBoxOption, mActivity, mGameDBAdapter);

            }
        });

        if (mActivity == EDIT_GAME) {
            setEnabled(false);
        }
    }

    @Override
    public Option.OptionID getID() {
        return mCheckBoxOption.getmID();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mCheckBox.setChecked(mCheckBoxOption.isChecked());
        mCheckBox.setEnabled(enabled);
    }

    @Override
    public void setOption(Option option) {
        mCheckBoxOption = (CheckBoxOption) option;
        mCheckBox.setChecked(mCheckBoxOption.isChecked());

        if (mActivity == EDIT_GAME) {
            setEnabled(false);
        }
    }
}

