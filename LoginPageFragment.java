package com.example.rachelfeddersen.testapp5;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by Rachel Feddersen on 11/7/2016.
 * This class creates the display for the Login Page
 */

public class LoginPageFragment extends Fragment {
    static CallableStatement cs;
    private EditText userEmail;

    public LoginPageFragment() {
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

        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        userEmail = (EditText) rootView.findViewById(R.id.loper_email);
        Button closeButton = (Button) rootView.findViewById(R.id.login_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();
                final String email = userEmail.getText().toString();

                // If all of the fields have been filled in
                if (!(email.equals(""))) {

                    AsyncTask<Object, Object, Object> studentInsert = new AsyncTask<Object, Object, Object>() {
                        private String message = "";

                        @Override
                        protected Object doInBackground(Object... params) {
                            try {
                                // Connect to the database
                                Connection conn = db.getConnection();

                                cs = conn.prepareCall("{Call trad_user_login(?,?)}");
                                cs.setString(1, email);
                                cs.registerOutParameter(2, Types.VARCHAR);
                                cs.executeQuery();
                                message = cs.getString(2);

                            }catch (SQLException ex){
                                ex.printStackTrace();
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Object result) {
                            // Hide the keyboard
                            ((MainActivity)getActivity()).hideKeyboard(getContext());

                            // If the log in was successful
                            if (message.equals("Login complete!")) {
                                // Set the user email so it can be used later
                                ((MainActivity)getActivity()).setUser(email);
                            }

                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setTitle("Login Results");
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
                } else {
                    // Hide the keyboard
                    ((MainActivity)getActivity()).hideKeyboard(getContext());

                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Empty fields!");
                    alertDialog.setMessage("A user name must be entered to log in!");
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

        return rootView;
    }
}
