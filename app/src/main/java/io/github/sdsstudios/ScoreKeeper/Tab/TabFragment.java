package io.github.sdsstudios.ScoreKeeper.Tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static android.view.View.INVISIBLE;

/**
 * Created by seth on 15/01/17.
 */
public class TabFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public TabFragment() {
    }

    public static TabFragment newInstance(int sectionNumber) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = null;
        container.setVisibility(INVISIBLE);
        return rootView;
    }

}
