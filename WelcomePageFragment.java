package com.example.rachelfeddersen.testapp5;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Rachel Feddersen on 11/3/2016.
 * This class creates the display for the Welcome Page
 */

public class WelcomePageFragment extends Fragment {

    public WelcomePageFragment() {
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

        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        return rootView;
    }
}
