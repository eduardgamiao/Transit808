package ics466uhm.transit808;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class BusStopSearchActivity extends ActionBarActivity {
    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private ListView listView;
    BusStopAdapter adapter;
    EditText editText;

    // Variables for sending to details activity.
    public final static String STREET_NAME_MESSAGE = "ics466uhm.transit808.STREET";
    public final static String BUS_STOP_ID = "ics466uhm.transit808.STOP_ID";
    public final static String STOP_COORDINATES = "ics466uhm.transit808.COORDINATES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);

        ArrayList stops = (ArrayList) getStops();

        listView = (ListView) findViewById(R.id.results);
        editText = (EditText) findViewById(R.id.inputSearch);

        // Adding items to list view.
        //adapter = new ArrayAdapter<BusStop>(this, R.layout.list_item, R.id.busStop, stops);
        adapter = new BusStopAdapter(this, R.layout.stop_list_item, stops);
        listView.setAdapter(adapter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BusStopSearchActivity.this.adapter.getFilter().filter(s);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Toast.makeText(getApplicationContext(),
                         parent.getAdapter().getItem(position).toString(),
                         Toast.LENGTH_SHORT).show();
                BusStop stop = (BusStop) parent.getAdapter().getItem(position);
                Intent intent = new Intent(BusStopSearchActivity.this, StopDetails.class);
                intent.putExtra(STREET_NAME_MESSAGE, stop.getStreetName());
                intent.putExtra(BUS_STOP_ID, stop.getStopID());
                startActivity(intent);
            }
        });

        // Navigation drawer.
        mDrawerList = (ListView) findViewById(R.id.navList);
        addDrawerItems();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        setupDrawer();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                Intent intent = null;
                switch(position) {
                    case 0:
                        intent = new Intent(BusStopSearchActivity.this, MainActivity.class);
                        break;
                    case 1:
                        break;
                    case 2:
                        intent = new Intent(BusStopSearchActivity.this, Trips.class);
                        break;
                    default:
                        break;
                }

                if (intent != null) {
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_stop_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private List<BusStop> getStops() {
        List<BusStop> stops = new ArrayList<BusStop>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open("stops.txt")));
            String currentLine;
            String []lineArray;
            while ((currentLine = br.readLine()) != null) {
                lineArray = currentLine.split(",");
                stops.add(new BusStop(lineArray[0] + "," + lineArray[2], lineArray[7], lineArray[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stops;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Navigation drawer setup.
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has setlled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getResources().getString(R.string.nav_title));
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Populate navigation drawer.
     */
    private void addDrawerItems() {
        String[] osArray = {"Home", "Arrival Times", "Trips"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }
}
