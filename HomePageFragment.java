package com.example.rachelfeddersen.testapp5;

/**
 * Created by Rachel Feddersen on 10/12/2016.
 * This class creates the display for the Home Page
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
public class HomePageFragment extends Fragment {

    public HomePageFragment() {
    }

    /**
     * This is the main method for the class
     * @param inflater - The object that displays the view
     * @param container - The object that the view is inflated into
     * @param savedInstanceState - The previous state of arguments from the program that called this one
     * @return - The view that was created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_page, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        createHomePage(rootView);

        return rootView;
    }

    /**
     * Creates the set up of the main page of the app
     * @param rootView - The view of the page
     */
    public void createHomePage(View rootView){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Create the image view for the top of the page
        ImageView homePageImage = (ImageView) rootView.findViewById(R.id.home_page_image);
        homePageImage.setImageResource(R.drawable.tradition_home_img);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth, (int) (screenHeight/2.75));
        params.gravity = Gravity.TOP;
        homePageImage.setLayoutParams(params);
    }
}
