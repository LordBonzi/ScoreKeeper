package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by Seth on 07/11/2016.
 */

public class LoadHistory extends AsyncTask<Context, Void ,List<HistoryModel>> {

    @Override
    protected List<HistoryModel> doInBackground(Context... ctx) {
        return HistoryModel.getHistoryModelList(new ScoreDBAdapter(ctx[0]).open(), ctx[0]);
    }

}
