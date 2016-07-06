package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

public class PlaceholderFragment extends Fragment implements HistoryAdapter.ViewHolder.ClickListener{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private ActionMode actionMode;
    private RecyclerView recyclerViewHome;
    ScoreDBAdapter dbHelper = new ScoreDBAdapter(getActivity());
    private HistoryAdapter historyAdapter;
    private static ArrayList<GameModel> gameModel;
    private GameModel gModel;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private UpdateTabsListener updateTabsListener = ((UpdateTabsListener)getActivity());

    public PlaceholderFragment() {
    }

    public static PlaceholderFragment newInstance(int sectionNumber) {PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        gModel.closeDB();
        dbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHelper.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewHome = (RecyclerView)rootView.findViewById(android.R.id.list);
        TextView textViewHome = (TextView)rootView.findViewById(R.id.textViewNoGames);
        RelativeLayout fragmentHomeLayout = (RelativeLayout) getActivity().findViewById(R.id.fragmentHomeLayout);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewHome.setLayoutManager(mLayoutManager);
        dbHelper = new ScoreDBAdapter(getActivity());
        dbHelper.open();
        gModel = new GameModel(dbHelper);

        try {
            if (dbHelper.numRows() != 0) {
                switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                    case 1:
                        textViewHome.setText(R.string.games_in_progress);
                        break;

                    case 2:
                        textViewHome.setText(R.string.completed_games);
                        break;

                    case 3:
                        textViewHome.setText(R.string.all_games);
                        break;

                }

                gameModel = GameModel.createGameModel(dbHelper.numRows(), getArguments().getInt(ARG_SECTION_NUMBER), getActivity());
                historyAdapter = new HistoryAdapter(gameModel, getActivity(), gameModel.size(), this, getArguments().getInt(ARG_SECTION_NUMBER));
                recyclerViewHome.setAdapter(historyAdapter);

            } else {

                switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                    case 1:
                        textViewHome.setText(R.string.games_in_progress);
                        break;

                    case 2:
                        textViewHome.setText(R.string.completed_games);
                        break;

                    case 3:
                        final String s = getResources().getString(R.string.all_games) + ":";
                        textViewHome.setText(s);
                        break;

                }

            }

        }catch (Exception e){
            e.printStackTrace();
            FirebaseCrash.report(new Exception(e.toString()));

        }

        return rootView;
    }

    private void toggleSelection(int position, int gameID) {
        historyAdapter.toggleSelection(position, gameID);
        int count = historyAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            try {
                actionMode.invalidate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClicked(int position, int gameID) {

        if (actionMode != null) {
            toggleSelection(position, gameID);
            actionMode.setTitle(historyAdapter.getSelectedItemCount() + " items selected");
        }else{
            Intent intent = new Intent(getActivity(), EditGame.class);
            intent.putExtra("gameID", gameID);
            getActivity().startActivity(intent);
        }

    }

    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionBarCallback());
        }

        if (actionMode != null) {
            actionMode.setTitle(1 + " items selected");
        }

        UpdateTabsListener updateTabsListener = ((UpdateTabsListener)getActivity());
        updateTabsListener.multiSelectEnabled();
        toggleSelection(position, gameID);

        return true;
    }

    public class ActionBarCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.action_mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // TODO: actually remove items
                    Log.e("actionbarcallback", "menu_remove");
                    dbHelper.open();
                    historyAdapter.deleteSelectedGames(dbHelper);
                    dbHelper.close();

                    updateTabsListener = ((UpdateTabsListener)getActivity());
                    updateTabsListener.gamesDeleted();

                    mode.finish();

                    return true;

                default:

                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            historyAdapter.clearSelection();
            updateTabsListener = ((UpdateTabsListener) getActivity());
            updateTabsListener.multiSelectDisabled();

            actionMode = null;
        }
    }
}
