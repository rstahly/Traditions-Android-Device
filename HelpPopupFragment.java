package com.example.rachelfeddersen.testapp5;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

/**
 * Created by Rachel Feddersen on 11/17/2016.
 * This class creates the display for the Help Popup
 */

public class HelpPopupFragment extends Activity {

    /**
     * This is the main method for the class
     * @param savedInstanceState - The previous state of arguments from the program that called this one
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_help_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.5));

        Button closeButton = (Button) findViewById(R.id.help_done_button);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // This function closes Activity HelpPopupFragment
                // Hint: use Context's finish() method
                finish();
            }
        });
    }
}
