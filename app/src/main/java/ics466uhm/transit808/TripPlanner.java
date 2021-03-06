package ics466uhm.transit808;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TripPlanner extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private AutoCompleteTextView from;
    private AutoCompleteTextView to;

    private DirectionStepAdapter adapter;

    private List<String> resultList = new ArrayList<String>();
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private GoogleApiClient mGoogleApiClient;
    private String address = "";

    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private static final String PLACES_API_AUTOCOMPLETION = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String PLACES_API_DIRECTIONS = "https://maps.googleapis.com/maps/api/directions/json?";
    public static final String ORIGIN = "ics466uhm.transit808.ORIGIN";
    public static final String DESTINATION = "ics466uhm.transit808.DESTINATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_planner);
        buildGoogleApiClient();

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        from.setText(this.address);
        to.setText("");

        from.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.dropdown_item));
        to.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.dropdown_item));

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
                        intent = new Intent(TripPlanner.this, MainActivity.class);
                        break;
                    case 1:
                        intent = new Intent(TripPlanner.this, BusStopSearch.class);
                        break;
                    case 2:
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void loadCurrentLocation(View view) {
        if (this.address.isEmpty() || this.address.equals("")) {
            Toast.makeText(this, "Cannot retrieve current location", Toast.LENGTH_LONG).show();
        }
        else {
            from.setText(this.address);
        }
    }

    private void setCurrentLocation() {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            if (mGoogleApiClient.isConnected()) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    List<Address> addressList = geocoder.getFromLocation(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude(), 1);
                    if (addressList != null || addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        String output = "";
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            if (i == 0) {
                                output = address.getAddressLine(i);
                            }
                            else {
                                output = output.concat(", " + address.getAddressLine(i));
                            }
                        }

                        this.address = output;

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trips, menu);
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

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        setCurrentLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * ArrayAdapter for Google AutoComplete.
     * https://developers.google.com/places/training/autocomplete-android
     */
    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        /**
         * Constructor.
         * @param context
         * @param textViewResourceId
         */
        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null && constraint.length() != 0) {
                        resultList = autocomplete(constraint.toString());

                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }

        private ArrayList<String> autocomplete(String s) {
            ArrayList<String> resultList = new ArrayList<String>();

            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest httpRequest) throws IOException {
                        httpRequest.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                HttpRequest request = requestFactory.buildGetRequest(buildURLAutoComplete(s));
                HttpResponse httpResponse = request.execute();
                PlaceResult directionResult = httpResponse.parseAs(PlaceResult.class);

                List<Prediction> predictions = directionResult.predictions;
                for (Prediction prediction : predictions) {
                    resultList.add(prediction.description);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultList;
        }

        private GenericUrl buildURLAutoComplete(String input) {
            // Build URL.
            GenericUrl url = new GenericUrl(PLACES_API_AUTOCOMPLETION);
            url.put("input", input);
            url.put("key", getResources().getString(R.string.google_browser_key));
            url.put("sensor", false);
            return url;
        }
    }

    public void createTrip(View view) {
        EditText from = (EditText) findViewById(R.id.from);
        EditText to = (EditText) findViewById(R.id.to);

        String fromText = from.getText().toString();
        String toText = to.getText().toString();

        Log.i("TRIP", fromText + " -> " + toText);

        if (fromText.isEmpty() && toText.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.missingInput), Toast.LENGTH_LONG).show();
        }
        else if (fromText.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.missingOrigin), Toast.LENGTH_LONG).show();
        }
        else if (toText.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.missingDestination), Toast.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(TripPlanner.this, TripDirections.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("trip", new Trip(from.getText().toString(), to.getText().toString(), ""));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void clearFrom(View view) {
        EditText from = (EditText) findViewById(R.id.from);
        from.setText("");
    }

    public void clearTo(View view) {
        EditText to = (EditText) findViewById(R.id.to);
        to.setText("");
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

    public static class PlaceResult {
        @Key("predictions")
        public List<Prediction> predictions;

    }

    public static class Prediction {
        @Key("description")
        public String description;

        @Key("id")
        public String id;
    }
}
