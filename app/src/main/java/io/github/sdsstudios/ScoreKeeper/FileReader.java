package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by seth on 23/02/17.
 */

public class FileReader {
    public static final String CHANGELOG = "CHANGELOG.txt";
    public static final String LICENSE = "LICENSE.txt";
    public static final String WHATS_NEW = "WHATS_NEW.txt";

    public static String textFromFileToString(String fileName, Context ctx) {

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(ctx.getAssets().open(fileName)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {

            Toast.makeText(ctx, "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return text.toString();

    }

}
