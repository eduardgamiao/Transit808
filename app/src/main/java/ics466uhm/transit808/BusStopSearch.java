package ics466uhm.transit808;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;


public class BusStopSearch extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private ListView listView;
    BusStopAdapter adapter;
    EditText editText;
    private GoogleMap googleMap;
    private boolean isTextVisible = true;
    private boolean isMapVisible = true;
    private GoogleApiClient mGoogleApiClient;
    private LatLng location;
    private List<LatLng> markers = new ArrayList<LatLng>();
    private HashMap<String, BusStop> markerMap = new HashMap<String, BusStop>();

    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

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
                BusStopSearch.this.adapter.getFilter().filter(s);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        parent.getAdapter().getItem(position).toString(),
                        Toast.LENGTH_SHORT).show();
                BusStop stop = (BusStop) parent.getAdapter().getItem(position);
                Intent intent = new Intent(BusStopSearch.this, StopDetails.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("stop", stop);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        buildGoogleApiClient();
        setCurrentLocation();

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
                        intent = new Intent(BusStopSearch.this, MainActivity.class);
                        break;
                    case 1:
                        break;
                    case 2:
                        intent = new Intent(BusStopSearch.this, TripPlanner.class);
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

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
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

    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        LinearLayout textLayout = (LinearLayout) findViewById(R.id.textSection);
        LinearLayout mapLayout = (LinearLayout) findViewById(R.id.mapSection);

        if (textLayout != null && mapLayout != null) {
            if (on) {
                Log.i("STATE", "MAP");
                textLayout.setVisibility(View.GONE);
                mapLayout.setVisibility(View.VISIBLE);
                setCurrentLocation();
            } else {
                Log.i("STATE", "TEXT");
                textLayout.setVisibility(View.VISIBLE);
                mapLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void setCurrentLocation() {
            if (mGoogleApiClient.isConnected()) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                        NearbyStops nearbyStops = new NearbyStops(this);
                        nearbyStops.execute(location.latitude + "," + location.longitude);
                }
                else {
                    Toast.makeText(this, "Cannot retrieve current location.", Toast.LENGTH_LONG).show();
                }
            }
    }

    private class NearbyStops extends AsyncTask<String, Integer, String> {
        private Context context;

        public NearbyStops(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest httpRequest) throws IOException {
                        httpRequest.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                HttpRequest request = requestFactory.buildGetRequest(buildURL(params[0]));
                HttpResponse httpResponse = request.execute();
                PlacesResult places = httpResponse.parseAs(PlacesResult.class);
                if (places.status.equals("OK")) {
                    int results = places.placeResults.size();
                    for (int i = 0; i < results; i++) {
                        String name = places.placeResults.get(0).name;
                        double latitude = places.placeResults.get(i).geometry.placeLocation.latitude;
                        double longitude = places.placeResults.get(i).geometry.placeLocation.longitude;
                        markers.add(new LatLng(latitude, longitude));
                    }
                }
                else {
                    Toast.makeText(context, "Could not find bus stops.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "SUCCESS";
        }

        protected void onPostExecute(String result) {
            if (googleMap != null) {
                for (LatLng current : markers) {
                    BusStop stop = getStop(current.latitude, current.longitude);
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(current)
                            .title(stop.getStreetName()).snippet("View Bus Stop"));
                    markerMap.put(marker.getId(), stop);
                }
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        BusStop stop = markerMap.get(marker.getId());
                        if (stop != null) {
                            Intent intent = new Intent(BusStopSearch.this, StopDetails.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("stop", stop);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    private GenericUrl buildURL(String location) {
        // Build URL.
        GenericUrl url = new GenericUrl(PLACES_URL);
        url.put("location", location);
        url.put("radius", 500);
        url.put("sensor", true);
        url.put("types", "bus_station");
        url.put("key", this.getResources().getString(R.string.google_browser_key));
        Log.i("URL", url.toString());
        return url;
    }

    private BusStop getStop(double latitude, double longitude) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open("stops.txt")));
            String currentLine;
            String []lineArray;
            while ((currentLine = br.readLine()) != null) {
                lineArray = currentLine.split(",");
                double currentLatitude = Double.parseDouble(lineArray[0]);
                double currentLongitude = Double.parseDouble(lineArray[2]);
                if (latitude == currentLatitude && longitude == currentLongitude) {
                    return new BusStop(lineArray[0] + "," + lineArray[2], lineArray[7], lineArray[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

    public void saveStop(View view) {
        Toast.makeText(getApplicationContext(),
               "ID: " + view.getId(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static class PlacesResult {
        @Key("status")
        public String status;

        @Key("results")
        public List<PlaceResult> placeResults;
    }

    public static class PlaceResult {
        @Key("geometry")
        public Geometry geometry;

        @Key("name")
        public String name;
    }

    public static class Geometry {
        @Key("location")
        public PlaceLocation placeLocation;
    }

    public static class PlaceLocation {
        @Key("lat")
        public double latitude;

        @Key("lng")
        public double longitude;
    }
}
