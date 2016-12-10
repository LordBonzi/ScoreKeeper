package io.github.sdsstudios.ScoreKeeper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by seth on 04/12/16.
 */

class DialogHelper {

    static void createAlertDialog(Context ctx, String title, String message){

        AlertDialog alertDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    static void textViewAlertDialog(Context ctx, String text){

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(ctx);
        final android.support.v7.app.AlertDialog alertDialog;
        final android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(ctx);
        dialogView = inflter.inflate(R.layout.changelog_fragment, null);

        TextView textView = (TextView)dialogView.findViewById(R.id.textViewChangelog);
        textView.setText(text);

        dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }

        });

        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.show();

    }
}
