package com.example.rachelfeddersen.testapp5;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Rachel Feddersen on 11/16/2016.
 * This class creates the display for the Contact Us Page
 */

public class ContactUsFragment extends Fragment implements OnMapReadyCallback {

    public ContactUsFragment() {
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

        View rootView = inflater.inflate(R.layout.fragment_contact_us, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        // Set up the map in this view
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.contact_us_map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    /**
     * This method puts a marker on the location where the alumni association is
     * @param googleMap - The map that is displayed on the page
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MarkerOptions opts = new MarkerOptions();

        // Latitude and longitude for 214 W 39th St. Kearney, NE - the geocoder does not work all the time so I had to go with hardcoded values
        LatLng location = new LatLng(40.7140653, -99.0842699);

        opts.position(location);
        opts.snippet("214 W 39th St. Kearney, NE");
        googleMap.addMarker(opts).setTitle("UNK Alumni Association");

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(location, 15.0f);

        googleMap.moveCamera(center);
    }
}
