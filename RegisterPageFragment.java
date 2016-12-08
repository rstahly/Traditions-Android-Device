package com.example.rachelfeddersen.testapp5;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Created by Rachel Feddersen on 10/24/2016.
 * This class creates the display for the Register Page
 */

public class RegisterPageFragment extends Fragment {
    static CallableStatement cs;
    private EditText userFirstName;
    private EditText userLastName;
    private EditText userEmail;
    private EditText userGradYear;
    private Spinner spinner;

    public RegisterPageFragment() {
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

        final View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        ArrayList<String> testarray = new ArrayList<>();
        testarray.add("Est. Grad. Month");
        testarray.add("December");
        testarray.add("May");
        testarray.add("July");

        spinner = (Spinner) rootView.findViewById(R.id.graduation_month);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.simple_spinner_dropdown_item, testarray) {

            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            public boolean areAllItemsEnabled() {
                return false;
            }
        };

        // Get all of the views from the page
        userFirstName = (EditText) rootView.findViewById(R.id.first_name);
        userLastName = (EditText) rootView.findViewById(R.id.last_name);
        userEmail = (EditText) rootView.findViewById(R.id.register_email);
        userGradYear = (EditText) rootView.findViewById(R.id.graduation_year);
        Button registerButton = (Button) rootView.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Set all of the variables
                final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();
                final String firstName = userFirstName.getText().toString();
                final String lastName = userLastName.getText().toString();
                final String email = userEmail.getText().toString();
                int gradMonth;
                int gradYear;

                // If the user selected December
                if (spinner.getSelectedItem().toString().equals("December")) {
                    gradMonth = 12;
                    // If the user selected May
                } else if (spinner.getSelectedItem().toString().equals("May")) {
                    gradMonth = 5;
                    // If the user selected July
                } else if (spinner.getSelectedItem().toString().equals("July")) {
                    gradMonth = 7;
                    // If the user never selected a month
                } else {
                    gradMonth = 0;
                }

                // If graduation year is equal to blank
                if (userGradYear.getText().toString().equals("")) {
                    gradYear = 0;
                } else {
                    gradYear = Integer.parseInt(userGradYear.getText().toString());
                }

                // If all of the fields have been filled in and it is a valid UNK email
                if ((!(firstName.equals(""))
                        && !(lastName.equals(""))
                        && !(email.equals(""))
                        && (gradMonth != 0)
                        && (gradYear != 0))
                        && (email.contains("@unk.edu")
                        || email.contains("@lopers.unk.edu"))) {

                    final int gradM = gradMonth;
                    final int gradY = gradYear;

                    // Create the async task for the student insert procedure
                    AsyncTask<Object, Object, Object> studentInsert = new AsyncTask<Object, Object, Object>() {
                        private String message = "";

                        @Override
                        protected Object doInBackground(Object... params) {
                            try {
                                // Connect to the database
                                Connection conn = db.getConnection();

                                cs = conn.prepareCall("{Call trad_student_add(?,?,?,?,?,?)}");
                                cs.setString(1, firstName);
                                cs.setString(2, lastName);
                                cs.setString(3, email);
                                cs.setInt(4, gradM);
                                cs.setInt(5, gradY);
                                cs.registerOutParameter(6, Types.VARCHAR);
                                cs.executeQuery();
                                message = cs.getString(6);

                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            // Hide the keyboard
                            ((MainActivity) getActivity()).hideKeyboard(getContext());

                            // If the registration for the user was successful
                            if (message.equals("Student Added")) {
                                // Log the user in
                                ((MainActivity) getActivity()).setUser(email);
                            }

                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setTitle("Registration Results");
                            alertDialog.setMessage(message);
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    };

                    studentInsert.execute((Object[]) null);
                } else if (!(email.contains("@unk.edu") || email.contains("@lopers.unk.edu"))) {
                    // Hide the keyboard
                    ((MainActivity)getActivity()).hideKeyboard(getContext());

                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Invalid Email!");
                    alertDialog.setMessage("You must have a valid UNK email address to register with this app.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
                    // Hide the keyboard
                    ((MainActivity)getActivity()).hideKeyboard(getContext());

                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Empty fields!");
                    alertDialog.setMessage("All of the fields must be filled and a graduation month must be chosen to register.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        return rootView;
    }
}
