package com.example.rachelfeddersen.testapp5;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Created by Rachel Feddersen on 10/29/2016.
 * This class creates the display for the Tradition Detail Page
 */

public class TraditionDetailFragment extends Fragment {
    private int traditionNumber;
    private String traditionPreview;
    private String traditionSummary;
    private String traditionInstruction;
    private InputStream traditionPhoto;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;
    static CallableStatement cs;

    public TraditionDetailFragment() {
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

        final View rootView = inflater.inflate(R.layout.fragment_tradition_detail, container, false);

        // Hide the keyboard
        ((MainActivity)getActivity()).hideKeyboard(getContext());

        savedInstanceState = getArguments();
        traditionNumber = savedInstanceState.getInt("traditionNum");

        final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();

        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    // Connect to the database
                    Connection conn = db.getConnection();

                    Statement stmt = conn.createStatement();
                    String query = "SELECT TraditionNumber, TraditionName, TraditionDescription, TraditionInstructions, TraditionPhoto FROM Tradition "
                        + "WHERE TraditionNumber = " + traditionNumber;
                    ResultSet rs = stmt.executeQuery(query);

                    rs.next();

                    traditionPreview = rs.getString(2);
                    traditionSummary = rs.getString(3);
                    traditionInstruction = rs.getString(4);
                    traditionPhoto = rs.getBinaryStream(5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                createDisplay(rootView);
            }
        }.execute(1);

        Button completeButton = (Button) rootView.findViewById(R.id.complete_tradition_button);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        return rootView;
    }

    /**
     * This method creates the initial display when the row is clicked on
     * @param rootView - The current view
     */
    private void createDisplay(View rootView) {
        // Set the image for the tradition detail
        ImageView img = (ImageView) rootView.findViewById(R.id.tradition_photo);
        img.setImageBitmap(BitmapFactory.decodeStream(traditionPhoto));

        // Set the summary of the tradition
        TextView preview = (TextView) rootView.findViewById(R.id.tradition_title);
        preview.setText(traditionPreview);

        // Set the summary of the tradition
        TextView summary = (TextView) rootView.findViewById(R.id.tradition_summary);
        summary.setText(traditionSummary);

        // Set the instructions for the tradition
        TextView instruction = (TextView) rootView.findViewById(R.id.tradition_instruction);
        instruction.setText(traditionInstruction);
    }

    /**
     * This method prompts the user for whether they want to take a picture or use one in the gallery
     */
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getContext());
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * This method starts the intent for the user taking a photo
     */
    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * This method starts the intent for the user selecting an image from the gallery
     */
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    /**
     * This method ensures that the app has permission to the device
     * @param requestCode - The code that deals with the app's permissions
     * @param permissions - The array of permissions
     * @param grantResults - The array of results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                }
                break;
        }
    }

    /**
     * This method calls the appropriate method depending on what the user selected
     * @param requestCode - The choice that the user made about how they want to complete the tradition
     * @param resultCode - The code that is used for the super call of this function
     * @param data - The Intent that was started when the user chose to complete the tradition
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        byte[] bArray = null;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                bArray = onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                bArray = onCaptureImageResult(data);
            }
        }

        completeTraditions(bArray);
    }

    /**
     * This method gets the data from the image if a user selected an image
     * @param data - The Intent that was started when the user chose this option
     * @return - The bytes that make up the image
     */
    @SuppressWarnings("deprecation")
    private byte[] onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bm = getResizedBitmap(bm, 220);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        return bytes.toByteArray();
    }

    /**
     * This method gets the data from the image if a user took a picture
     * @param data - The Intent that was started when the user chose this option
     * @return - The bytes that make up the image
     */
    private byte[] onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes.toByteArray();
    }

    /**
     * This method re-sizes the image so it can be sent to the database
     * @param image - The previous image
     * @param maxSize - The max length or height of the picture
     * @return - The scaled down image
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * This method calls the procedure in the database to allow the user to complete the tradition
     * @param b - The bytes that make up the image the user selected
     */
    private void completeTraditions(byte[] b) {
        // If an image was returned
        if (b != null) {
            final byte[] imageArray = b;
            final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();

            new AsyncTask<Integer, Void, Void>(){
                private String message = "";

                @Override
                protected Void doInBackground(Integer... params) {
                    try {
                        String userName = ((MainActivity)getActivity()).getUser();
                        // Connect to the database
                        Connection conn = db.getConnection();

                        cs = conn.prepareCall("{Call trad_complete_proc(?,?,?,?)}");
                        cs.setBytes(1, imageArray);
                        cs.setString(2, userName);
                        cs.setInt(3, traditionNumber);
                        cs.registerOutParameter(4, Types.VARCHAR);
                        cs.executeQuery();
                        message = cs.getString(4);

                    }catch (SQLException ex){
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (message.equals("Tradition Completed!")) {
                        studentLevelCheck();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Completed Tradition Results");
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            }.execute(1);
        }
    }

    /**
     * This method checks to see if the user has completed a certain number of traditions
     */
    private void studentLevelCheck() {
        AsyncTask<Object, Object, Object> checkLevel = new AsyncTask<Object, Object, Object>() {
            final DatabaseConnector db = ((MainActivity)getActivity()).getDatabaseConnection();
            private String message = "";

            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String userName = ((MainActivity)getActivity()).getUser();
                    // Connect to the database
                    Connection conn = db.getConnection();

                    cs = conn.prepareCall("{Call check_level_proc(?,?)}");
                    cs.setString(1, userName);
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
                // If the user has completed x number of traditions
                if (!(message.equals(""))) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Tradition Goal Results");
                    alertDialog.setMessage(message);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                // If the user has not completed x number of traditions
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Completed Tradition Results");
                    alertDialog.setMessage("Tradition Completed!");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        };

        checkLevel.execute((Object[]) null);
    }
}
