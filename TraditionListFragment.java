package com.example.rachelfeddersen.testapp5;

/**
 * Created by Rachel Feddersen on 10/12/2016.
 * This class creates the display for the Tradition List Page
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class TraditionListFragment extends Fragment {
    private ArrayList<String> traditionTitles;
    private ArrayList<InputStream> traditionThumbs;
    private ArrayList<Integer> traditionComplete;
    private TableLayout traditionList;

    public TraditionListFragment() {
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

        final View rootView = inflater.inflate(R.layout.fragment_tradition_list, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        traditionTitles = new ArrayList<>();
        traditionThumbs = new ArrayList<>();
        traditionComplete = new ArrayList<>();

        final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();

        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    // Connect to the database
                    Connection conn = db.getConnection();

                    String userName = ((MainActivity) getActivity()).getUser();

                    Statement stmt = conn.createStatement();
                    String sqlStmt = "SELECT TraditionNumber, TraditionName, TraditionThumbnail, "
                            + "CASE WHEN (SELECT TraditionID FROM CompletedTradition "
                            + "WHERE StudentID IN "
                            + "(SELECT StudentID FROM Student WHERE StudentEmail = '" + userName + "') "
                            + "AND TraditionID = TraditionNumber) "
                            + "IS NOT NULL THEN 1 ELSE 0 END AS Completed FROM Tradition";
                    ResultSet rs = stmt.executeQuery(sqlStmt);

                    // Add column values to array lists
                    while (rs.next())
                    {
                        traditionTitles.add(rs.getInt(1)-1, rs.getString(2));
                        traditionThumbs.add(rs.getInt(1)-1, rs.getBinaryStream(3));
                        traditionComplete.add(rs.getInt(1)-1, rs.getInt(4));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                createRows(rootView);
            }
        }.execute(1);

        return rootView;
    }

    /**
     * Creates the rows for the tradition list page and puts them in the table layout
     * @param rootView - The view of the page
     */
    public void createRows(View rootView) {
        // Get the dimensions and information about the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        traditionList = (TableLayout) rootView.findViewById(R.id.tradition_list_table);
        traditionList.removeAllViews();

        rootView.setPadding(0, 0, 10, 0);

        for (int i = 0; i < traditionThumbs.size(); i++) {
            // Create a table row and give it an ID
            TableRow row= new TableRow(rootView.getContext());
            row.setId(i);

            // Create parameters for the layout and add it to the row
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 300);
            row.setLayoutParams(lp);

            // Create an image view and set its parameters
            ImageView traditionThumbnail = new ImageView(rootView.getContext());
            traditionThumbnail.setLayoutParams(new TableRow.LayoutParams(300, 300));
            Drawable traditionImage = new BitmapDrawable(BitmapFactory.decodeStream(traditionThumbs.get(i)));

            // If the tradition has already been completed
            if (traditionComplete.get(i) == 1) {
                // Create a layer of the complete filter over the image
                Resources r = getResources();
                Drawable[] layers = new Drawable[2];
                layers[0] = traditionImage;
                layers[1] = r.getDrawable(R.mipmap.complete_filter);
                LayerDrawable traditionLayer = new LayerDrawable(layers);
                traditionThumbnail.setImageDrawable(traditionLayer);
            } else {
                traditionThumbnail.setImageDrawable(traditionImage);
            }

            // Create a text view and set its parameters
            TextView traditionPreview = new TextView(rootView.getContext());
            traditionPreview.setText(traditionTitles.get(i));
            TableRow.LayoutParams textLayoutParms = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 300);
            textLayoutParms.width = screenWidth-300;
            traditionPreview.setLayoutParams(textLayoutParms);

            // Set the image and text view to the row and add the row to the table
            row.addView(traditionThumbnail);
            row.addView(traditionPreview);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    // The getID method should get the row index
                    selectRow(v.getId());
                }
            });

            traditionList.addView(row,i);
        }
    }

    /**
     * This method is what calls the TraditionDetail program when a row is clicked in the list
     * @param position - The position in the table that the row was
     */
    private void selectRow(int position) {
        Fragment fragment = new TraditionDetailFragment();

        // Create a new bundle and store the tradition number in the bundle
        Bundle bundle = new Bundle();
        bundle.putInt("traditionNum", position+1);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        // Add the current fragment to the backstack so the user can press the back button to go back through the views
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "traditionList").addToBackStack("Tradition Detail").commit();
    }
}
