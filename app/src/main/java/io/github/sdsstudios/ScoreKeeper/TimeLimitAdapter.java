package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class TimeLimitAdapter extends BaseAdapter{

    public static String timeLimit;

    private DataHelper dataHelper;
    private int gameID;
    private List timeLimitArray;
    private List timeLimitArrayNum;
    LayoutInflater inflter;
    private NewGame newGame;
    private TextView timeLimitTextView;
    private Context context;
    private ScoreDBAdapter dbHelper;
    public static AlertDialog alertDialog = null;


    public TimeLimitAdapter(Context ctx,List objects, List objectsNum, ScoreDBAdapter db, int id) {
        context = ctx;
        timeLimitArray = objects;
        inflter = (LayoutInflater.from(ctx));
        dbHelper = db;
        gameID = id;
        timeLimitArrayNum = objectsNum;
    }

    @Override
    public int getCount() {
        return timeLimitArray.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        newGame = new NewGame();
        dataHelper = new DataHelper();

        view = inflter.inflate(R.layout.time_limit_spinner_adapter, null);

        timeLimitTextView = (TextView) view.findViewById(R.id.textView);
        timeLimitTextView.setText(timeLimitArray.get(position).toString());

        timeLimitTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (position != timeLimitArray.size() -1) {
                    timeLimitArray.remove(position);
                    timeLimitArrayNum.remove(position);
                    notifyDataSetChanged();
                    saveSharedPrefs(timeLimitArray, timeLimitArrayNum);
                }

                return true;
            }
        });

        timeLimitTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (position < timeLimitArray.size() -1){
                    timeLimit = timeLimitArrayNum.get(position).toString();
                    NewGame.spinnerTimeLimit.setSelection(position);

                    Game g = dataHelper.getGame(gameID, dbHelper);
                    g.setmTimeLimit(timeLimit);
                    dbHelper.open().updateGame(g);
                }else {

                    final View dialogView;

                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogView = inflter.inflate(R.layout.create_time_limit, null);
                    EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
                    EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
                    EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
                    RelativeLayout relativeLayout = (RelativeLayout)dialogView.findViewById(R.id.relativeLayout2);
                    relativeLayout.setVisibility(View.VISIBLE);
                    final CheckBox checkBoxExtend = (CheckBox)dialogView.findViewById(R.id.checkBoxExtend);
                    checkBoxExtend.setVisibility(View.INVISIBLE);
                    editTextHour.setText("0");
                    editTextMinute.setText("0");
                    editTextSecond.setText("0");

                    dialogBuilder.setPositiveButton(R.string.create, null);
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });

                    dialogBuilder.setView(dialogView);

                    alertDialog = dialogBuilder.create();

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
                                    final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
                                    final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
                                    String hour = editTextHour.getText().toString().trim();
                                    String minute = editTextMinute.getText().toString().trim();
                                    String seconds = editTextSecond.getText().toString().trim();
                                    String timeLimitString = "";

                                    if (TextUtils.isEmpty(hour)) {
                                        editTextHour.setError("Can't be empty");
                                        return;
                                    } else if (TextUtils.isEmpty(minute)) {
                                        editTextMinute.setError("Can't be empty");
                                        return;
                                    } else if (TextUtils.isEmpty(seconds)) {
                                        editTextSecond.setError("Can't be empty");
                                        return;
                                    } else {

                                        if (Integer.valueOf(hour) >= 24) {
                                            editTextHour.setError("Hour must be less than 24");
                                        } else if (Integer.valueOf(minute) >= 60) {
                                            editTextMinute.setError("Minute must be less than 60");

                                        } else if (Integer.valueOf(seconds) >= 60) {
                                            editTextSecond.setError("Seconds must be less than 60");

                                        } else {

                                            try {
                                                if (hour.length() == 1 && !hour.equals("0")) {
                                                    hour = ("0" + hour);
                                                }
                                                if (minute.length() == 1 && !minute.equals("0")) {
                                                    minute = ("0" + minute);
                                                }
                                                if (seconds.length() == 1 && !seconds.equals("0")) {
                                                    seconds = ("0" + seconds);
                                                }

                                                if (hour.equals("0")) {
                                                    hour = "00";
                                                }

                                                if (minute.equals("0")) {
                                                    minute = "00";
                                                }

                                                if (seconds.equals("0")) {
                                                    seconds = "00";
                                                }

                                                timeLimitString += hour + ":";
                                                timeLimitString += minute + ":";
                                                timeLimitString += seconds + ":";
                                                timeLimitString += "0";

                                                if (!timeLimitString.equals("00:00:00:0")) {
                                                    dbHelper = new ScoreDBAdapter(context);

                                                    Game g = dataHelper.getGame(gameID, dbHelper);
                                                    g.setmTimeLimit(timeLimit);
                                                    dbHelper.open().updateGame(g);

                                                    timeLimit = timeLimitString;
                                                    timeLimitArray.add(timeLimitArray.size() - 1, dataHelper.createTimeLimitCondensed(timeLimitString));
                                                    timeLimitArrayNum.add(timeLimitArrayNum.size() - 1, timeLimitString);

                                                    if (dataHelper.checkDuplicates(timeLimitArray)) {
                                                        timeLimitArray.remove(timeLimitArray.size() - 2);
                                                        timeLimitArrayNum.remove(timeLimitArrayNum.size() - 2);
                                                        Snackbar snackbar = Snackbar.make(NewGame.relativeLayout, "Already exists", Snackbar.LENGTH_SHORT);
                                                        snackbar.show();

                                                    }

                                                    notifyDataSetChanged();
                                                    alertDialog.dismiss();
                                                    NewGame.spinnerTimeLimit.setSelection(timeLimitArray.size() - 2);
                                                    saveSharedPrefs(timeLimitArray, timeLimitArrayNum);

                                                } else {
                                                    alertDialog.dismiss();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e("tmelimitadapter", e.toString());
                                                Toast toast = Toast.makeText(context, R.string.invalid_time, Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        });
                    alertDialog.show();

                    }

    }
});
        return view;
    }

    public void saveSharedPrefs(List array, List arrayNum){
        DataHelper dataHelper =new DataHelper();
        SharedPreferences sharedPref = context.getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("timelimitarray", dataHelper.convertToString(array));
        editor.putString("timelimitarraynum", dataHelper.convertToString(arrayNum));

        editor.apply();
    }

}