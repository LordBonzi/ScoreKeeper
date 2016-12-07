package io.github.sdsstudios.ScoreKeeper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by seth on 04/12/16.
 */

public class DialogHelper {

    public static void createAlertDialog(Context ctx, String title, String message){

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
}
