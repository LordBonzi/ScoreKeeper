package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

public class Report extends AppCompatActivity implements View.OnClickListener{
    private Button buttonReportAction;
    private EditText editTextReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        setTheme(accentColor);
        setContentView(R.layout.activity_report);
        editTextReport = (EditText)findViewById(R.id.editTextReport);
        buttonReportAction = (Button)findViewById(R.id.buttonReportAction);
        buttonReportAction.setOnClickListener(this);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                FirebaseCrash.report(new Exception(e.toString()));

            }
        });

    }

    @Override
    public void onClick(View v) {
        FirebaseCrash.report(new Exception(editTextReport.getText().toString()));
        Toast toast = Toast.makeText(this, "Sent Report", Toast.LENGTH_SHORT);


    }
}
