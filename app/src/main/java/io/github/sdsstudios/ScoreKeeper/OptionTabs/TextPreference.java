package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.EditTextOption;
import io.github.sdsstudios.ScoreKeeper.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Option;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 17/12/16.
 */

public class TextPreference extends OptionPreference {

    private EditText mEditText;
    private TextView mTextViewTitle;
    private EditTextOption mEditTextOption;
    private Activity mActivity;
    private GameDBAdapter mGameDBAdapter;

    public TextPreference(Context context, EditTextOption editTextOption, Option.OptionListener optionListener
            , Activity activity, GameDBAdapter gameDBAdapter) {
        super(context, optionListener);

        setLayoutResource(R.layout.edit_text_option);

        this.mEditTextOption = editTextOption;
        this.mActivity = activity;
        this.mGameDBAdapter = gameDBAdapter;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mEditText = (EditText) holder.findViewById(R.id.editText);
        mTextViewTitle = (TextView) holder.findViewById(R.id.textViewTitle);

        mTextViewTitle.setText(mEditTextOption.getmHint());
        mEditText.setText(mEditTextOption.getString());

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {

                    /** if statement necessary to avoid numberformatexception if edittext empty **/

                    if (charSequence != "") {
                        mEditTextOption.setData(Integer.parseInt(String.valueOf(charSequence)));
                    } else {
                        mEditTextOption.setData(Integer.parseInt(mEditTextOption.getmDefaultValue()));
                    }

                } catch (NumberFormatException error) {
                    error.printStackTrace();
                    mEditTextOption.setData(Integer.parseInt(mEditTextOption.getmDefaultValue()));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mOptionListener.onOptionChange(mEditTextOption, mActivity, mGameDBAdapter);
            }

        });
    }

    @Override
    public Option.OptionID getID() {
        return mEditTextOption.getmID();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEditText.setText(mEditTextOption.getString());
        mEditText.setEnabled(enabled);
    }

    @Override
    public void setOption(Option option) {
        mEditTextOption = (EditTextOption) option;

        if (Integer.parseInt(mEditTextOption.getmDefaultValue()) != mEditTextOption.getInt()) {
            mEditText.setText(String.valueOf(mEditTextOption.getInt()));
        } else {
            mEditText.setText("");
        }

        if (mActivity == Activity.EDIT_GAME) {
            setEnabled(false);
        }
    }
}
