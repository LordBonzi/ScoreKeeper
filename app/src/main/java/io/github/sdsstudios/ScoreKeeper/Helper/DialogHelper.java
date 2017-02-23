package io.github.sdsstudios.ScoreKeeper.Helper;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 04/12/16.
 */

public class DialogHelper {

    public static void textViewAlertDialog(Context ctx, String text, String title) {

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(ctx);
        final AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(ctx);
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
        dialogBuilder.setTitle(title);

        dialogBuilder.create().show();
    }
}
