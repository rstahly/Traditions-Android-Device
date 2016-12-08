package com.example.rachelfeddersen.testapp5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Rachel Feddersen on 10/12/2016.
 * This class maintains all of the information for the app and controls the fragments
 */
public class MainActivity extends AppCompatActivity {

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mTitle;
    private DatabaseConnector db;
    private String userName;
    android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        setupToolbar();

        // Get the drawer item list and the titles for the navigation drawer
        DataModel[] drawerItem = new DataModel[getResources().getStringArray(R.array.navigation_drawer_items_array).length];
        String[] navigationTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);

        // Iterate through the list and set the images for each item
        for (int i = 0; i < getResources().getStringArray(R.array.navigation_drawer_items_array).length; i++) {
            switch (i) {
                case 0:
                    drawerItem[i] = new DataModel(R.mipmap.ic_home, navigationTitles[i]);
                    break;
                case 1:
                    drawerItem[i] = new DataModel(R.mipmap.ic_welcome, navigationTitles[i]);
                    break;
                case 2:
                    drawerItem[i] = new DataModel(R.mipmap.ic_register, navigationTitles[i]);
                    break;
                case 3:
                    drawerItem[i] = new DataModel(R.mipmap.ic_login, navigationTitles[i]);
                    break;
                case 4:
                    drawerItem[i] = new DataModel(R.mipmap.ic_traditions, navigationTitles[i]);
                    break;
                case 5:
                    drawerItem[i] = new DataModel(R.mipmap.ic_alumni, navigationTitles[i]);
                    break;
                case 6:
                    drawerItem[i] = new DataModel(R.mipmap.ic_contact, navigationTitles[i]);
                    break;
                case 7:
                    drawerItem[i] = new DataModel(R.mipmap.ic_help, navigationTitles[i]);
                    break;
                default:
                    break;
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupDrawerToggle();

        // Connect to the database using an async task
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    // Connect to the database
                    db = new DatabaseConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);

        // Call the selectItem method to display the home page initially
        selectItem(0);
    }

    /**
     * This method listens to the drawer clicks and calls the selectItem method
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    /**
     * This method uses fragments to go to the different pages based on a navigation drawer selection
     * @param position - The position of the item in the navigation drawer
     */
    private void selectItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new HomePageFragment();
                break;
            case 1:
                fragment = new WelcomePageFragment();
                break;
            case 2:
                fragment = new RegisterPageFragment();
                break;
            case 3:
                fragment = new LoginPageFragment();
                break;
            case 4:
                fragment = new TraditionListFragment();
                break;
            case 6:
                fragment = new ContactUsFragment();
                break;
            default:
                break;
        }

        // If a fragment was set
        if (fragment != null) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            // Add the current fragment to the backstack so the user can press the back button to go back through the views
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "myActivity").addToBackStack(mNavigationDrawerItemTitles[position]).commit();
            fragmentManager.addOnBackStackChangedListener(
                    new FragmentManager.OnBackStackChangedListener() {
                        // This method handles any back button presses
                        public void onBackStackChanged() {
                            int index = fragmentManager.getBackStackEntryCount() - 1;
                            if (index >= 0) {
                                String tag = fragmentManager.getBackStackEntryAt(index).getName();
                                int currentItem;
                                // This switch statement changes the title of the page based on what page was in the stack before
                                switch (tag) {
                                    case "Home":
                                        currentItem = 0;
                                        break;
                                    case "Welcome":
                                        currentItem = 1;
                                        break;
                                    case "Register":
                                        currentItem = 2;
                                        break;
                                    case "Login":
                                        currentItem = 3;
                                        break;
                                    case "Traditions":
                                        currentItem = 4;
                                        break;
                                    case "Contact Us":
                                        currentItem = 6;
                                        break;
                                    default:
                                        currentItem = 4;
                                        tag = "Traditions";
                                        break;
                                }
                                mDrawerList.setItemChecked(currentItem, true);
                                mDrawerList.setSelection(currentItem);
                                setTitle(tag);
                            }
                        }
                    });

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else if (position == 5) {
            // Pop all of the fragments in the stack when going to the alumni page
            fragment = new HomePageFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // Reset everything to be on the home back and then start a new intent to go to the alumni page
            mDrawerList.setItemChecked(0, true);
            mDrawerList.setSelection(0);
            setTitle(mNavigationDrawerItemTitles[0]);
            mDrawerLayout.closeDrawer(mDrawerList);
            // Open up the UNK Alumni Association web page in a browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://unkalumni.org/"));
            startActivity(intent);
        } else if (position == 7) {
            // Close the navigation drawer before opening the pop up
            mDrawerLayout.closeDrawer(mDrawerList);
            startActivity(new Intent(MainActivity.this, HelpPopupFragment.class));
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    /**
     * This method returns which item was selected
     * @param item - The item in the navigation drawer
     * @return - Whether or not the item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * This method sets the title of the page
     * @param title - The page's title
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * This method occurs after the app is created
     * @param savedInstanceState - The state of arguments from the program that called this one
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * This method calls the selectItem method based on what button or link was clicked by the user. It simulates a navigation drawer click
     * @param view - The button view that was clicked
     */
    public void buttonClicked(View view) {
        switch (view.getId()) {
            case R.id.welcome_button:
                selectItem(1);
                break;
            // Button for going from the login page to the register page
            case R.id.register_login_button:
                selectItem(2);
                break;
            case R.id.register_page_button:
                selectItem(2);
                break;
            case R.id.login_page_button:
                selectItem(3);
                break;
            case R.id.traditions_button:
                selectItem(4);
                break;
            case R.id.unk_alumni_button:
                selectItem(5);
                break;
            default:
                break;
        }
    }

    /**
     * This method sets up the toolbar
     */
    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * This method sets up the navigation drawer so it can be toggled
     */
    void setupDrawerToggle() {
        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    /**
     * This method gets the database connection and returns it
     * @return - The database connection
     */
    public DatabaseConnector getDatabaseConnection() {
        return db;
    }

    /**
     * This method sets the user name so it can be used again
     * @param user - The valid user name for who is currently logged in
     */
    public void setUser(String user) {
        userName = user;
    }

    /**
     * This method gets the user name and returns it
     * @return - The user name
     */
    public String getUser() {
        return userName;
    }

    /**
     * This method hides the keyboard when an event occurs or a different page is visited
     * @param ctx - The context so the keyboard can be found and hidden
     */
    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
