package ics466uhm.transit808;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    public static List<BusStop> savedStops = new ArrayList<BusStop>();

    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private static final String SAVED_STOPS = "SAVED_STOPS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, BusStopSearch.class);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, TripPlanner.class);
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

        populateSavedData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void switchToArrival(View view) {
        Intent intent = new Intent(this, BusStopSearch.class);
        startActivity(intent);
    }

    public void switchToTrips(View view) {
        Intent intent = new Intent(this, TripPlanner.class);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateSavedData();
    }

    /**
     * Navigation drawer setup.
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely open state.
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

    private void populateSavedData() {
        DatabaseHandler db = new DatabaseHandler(this);
        ArrayList<BusStop> stopList = db.getBusStops();
        ArrayList<Trip> tripList = db.getTrips();
        if (stopList.size() > 0) {
            BusStopAdapter adapter = new BusStopAdapter(this, R.layout.stop_list_item_main, stopList);
            ListView stops = (ListView) findViewById(R.id.saved_stops_list);
            stops.setVisibility(View.VISIBLE);
            TextView emptyText = (TextView) findViewById(R.id.saved_stops_empty);
            emptyText.setVisibility(View.GONE);
            stops.setAdapter(adapter);

            stops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BusStop stop = (BusStop) parent.getAdapter().getItem(position);
                    Intent intent = new Intent(MainActivity.this, StopDetails.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("stop", stop);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
        else {
            TextView emptyText = (TextView) findViewById(R.id.saved_stops_empty);
            emptyText.setVisibility(View.VISIBLE);
            ListView stops = (ListView) findViewById(R.id.saved_stops_list);
            stops.setVisibility(View.GONE);
        }
        if (tripList.isEmpty()) {
            TextView emptyText = (TextView) findViewById(R.id.saved_trips_empty);
            emptyText.setVisibility(View.VISIBLE);
            ListView stops = (ListView) findViewById(R.id.saved_trips_list);
            stops.setVisibility(View.GONE);
        }
        else {
            TripAdapter adapter = new TripAdapter(this, R.layout.trips, tripList);
            ListView trips = (ListView) findViewById(R.id.saved_trips_list);
            trips.setVisibility(View.VISIBLE);
            TextView emptyText = (TextView) findViewById(R.id.saved_trips_empty);
            emptyText.setVisibility(View.GONE);
            trips.setAdapter(adapter);

            trips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Trip trip = (Trip) parent.getAdapter().getItem(position);
                    Intent intent = new Intent(MainActivity.this, TripDirections.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("trip", trip);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    }
}
