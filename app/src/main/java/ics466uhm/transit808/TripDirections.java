package ics466uhm.transit808;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the directions of a trip.
 */
public class TripDirections extends ActionBarActivity {
    private Trip trip;

    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private static final String PLACES_API_DIRECTIONS = "https://maps.googleapis.com/maps/api/directions/json?";
    private LatLng startingCoordinates;
    private LatLng endingCoordinates;
    private List<LatLng> coordinatesList;
    private GoogleMap googleMap;
    private String overviewPolyline;

    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        trip = bundle.getParcelable("trip");
        createTrip(trip);
        TextView origin = (TextView) findViewById(R.id.origin);
        TextView destination = (TextView) findViewById(R.id.destination);
        origin.setText(trip.getOrigin());
        destination.setText(trip.getDestination());
        changeButtonState();

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
                        intent = new Intent(TripDirections.this, MainActivity.class);
                        break;
                    case 1:
                        intent = new Intent(TripDirections.this, BusStopSearchActivity.class);
                        break;
                    case 2:
                        intent = new Intent(TripDirections.this, TripPlanner.class);
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
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        trip = bundle.getParcelable("trip");
        createTrip(trip);
        TextView origin = (TextView) findViewById(R.id.origin);
        TextView destination = (TextView) findViewById(R.id.destination);
        origin.setText(trip.getOrigin());
        destination.setText(trip.getDestination());
        changeButtonState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);
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

    private class DirectionsFetcher extends AsyncTask<String, Integer, String> {
        private ArrayList<DirectionStep> tripDirections = new ArrayList<DirectionStep>();
        private Context context;

        public DirectionsFetcher(Context context) {
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

                HttpRequest request = requestFactory.buildGetRequest(buildURLDirections(params[0], params[1]));
                HttpResponse httpResponse = request.execute();
                DirectionsResult directions = httpResponse.parseAs(DirectionsResult.class);
                if (directions.status.equals("OK")) {
                    int steps = directions.routes.get(0).step.get(0).instruction.size();
                    for (int i = 0; i < steps; i++) {
                        String instruction = directions.routes.get(0).step.get(0).instruction.get(i)
                                .instructions;
                        String travelMode = directions.routes.get(0).step.get(0).instruction.get(i).travelMode;
                        startingCoordinates = new LatLng(directions.routes.get(0).step.get(0).startLocation.lat,
                                directions.routes.get(0).step.get(0).startLocation.lng);
                        endingCoordinates = new LatLng(directions.routes.get(0).step.get(0).endLocation.lat,
                                directions.routes.get(0).step.get(0).endLocation.lng);
                        overviewPolyline = directions.routes.get(0).polyline.overviewPolyline;
                        Log.i("COORDINATES", startingCoordinates.latitude + "|" + startingCoordinates.longitude);
                        if (travelMode != null && travelMode.equals("TRANSIT")) {
                            String departure = directions.routes.get(0).step.get(0).instruction.get(i)
                                    .details.departure.name;
                            String arrival = directions.routes.get(0).step.get(0).instruction.get(i)
                                    .details.arrival.name;
                            String route = directions.routes.get(0).step.get(0).instruction.get(i)
                                    .details.line.route;
                            String headsign = directions.routes.get(0).step.get(0).instruction.get(i)
                                    .details.headsign;
                            //LatLng coordinates = new LatLng(directions.routes.get(0).step.get(0).instruction.get(i)
                             //       .stepStartCoordinate.lat, directions.routes.get(0).step.get(0)
                              //      .instruction.get(i)
                               //     .stepStartCoordinate.lng);
                            //if (coordinates != startingCoordinates || coordinates != endingCoordinates) {
                             //   coordinatesList.add(coordinates);
                            //}
                            tripDirections.add(new DirectionStep(instruction, departure, arrival, route, headsign));
                        } else {
                            tripDirections.add(new DirectionStep(instruction));
                        }
                    }
                }
                else {
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, getResources().getString(R.string.invalid_trip), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "SUCCESS";
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            DirectionStepAdapter directionAdapter = new DirectionStepAdapter(TripDirections.this, tripDirections);
            ListView listView = (ListView) findViewById(R.id.directions);
            listView.setAdapter(directionAdapter);
            try {
                initializeMap();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<DirectionStep> getDirections() {
            return tripDirections;
        }
    }

    public ArrayList<DirectionStep> createTrip(Trip trip) {
        Log.i("Create", trip.getOrigin() + " -> " + trip.getDestination());

        DirectionsFetcher df = new DirectionsFetcher(this);

        df.execute(trip.getOrigin(), trip.getDestination());

        return df.getDirections();
    }

    private GenericUrl buildURLDirections(String origin, String destination) {
        // Build URL.
        GenericUrl url = new GenericUrl(PLACES_API_DIRECTIONS);
        url.put("origin", origin);
        url.put("destination", destination);
        url.put("sensor", false);
        url.put("mode", "transit");
        url.put("travel_mode", "bus");
        url.put("travel_routing_preference", "fewer_transfers");
        Log.i("URL_DIRECTIONS", url.toString());
        return url;
    }

    public void saveTrip(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.addTrip(trip);
        changeButtonState();
    }

    public void removeTrip(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteTrip(trip.getOrigin() + "|" + trip.getDestination());
        changeButtonState();
    }

    private void changeButtonState() {
        DatabaseHandler db = new DatabaseHandler(this);
        if (db.getTrip(trip.getOrigin(), trip.getDestination()) == null) {
            Button oldButton = (Button) findViewById(R.id.removeTrip);
            oldButton.setVisibility(View.GONE);
            Button newButton = (Button) findViewById(R.id.addTrip);
            newButton.setVisibility(View.VISIBLE);
        }
        else {
            Button oldButton = (Button) findViewById(R.id.removeTrip);
            oldButton.setVisibility(View.VISIBLE);
            Button newButton = (Button) findViewById(R.id.addTrip);
            newButton.setVisibility(View.GONE);
        }
    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Error creating map. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingCoordinates, 15));
                googleMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(overviewPolyline)).width(3));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.addMarker(new MarkerOptions().position(startingCoordinates).title(trip.getOriginShort()));
                googleMap.addMarker(new MarkerOptions().position(endingCoordinates).title(trip.getDestinationShort()));
            }
        }
    }

    public static class DirectionsResult {
        @Key("routes")
        public List<Route> routes;

        @Key("status")
        public String status;
    }

    public static class Route {
        @Key("legs")
        public List<Step> step;

        @Key("overview_polyline")
        public Polyline polyline;
    }

    public static class Polyline {
        @Key("points")
        public String overviewPolyline;
    }

    public static class Step {
        @Key("start_location")
        public StartLocation startLocation;

        @Key("end_location")
        public EndLocation endLocation;

        @Key("steps")
        public List<Instruction> instruction;
    }

    public static class StartLocation {
        @Key("lat")
        public double lat;

        @Key("lng")
        public double lng;
    }

    public static class EndLocation {
        @Key("lat")
        public double lat;

        @Key("lng")
        public double lng;
    }

    public static class Instruction {
        @Key("html_instructions")
        public String instructions;

        @Key("transit_details")
        public Detail details;

        @Key("travel_mode")
        public String travelMode;
    }

    public class StepStartLocation {
        @Key("lat")
        public double lat;

        @Key("lng")
        public double lng;
    }

    public static class Detail {
        @Key("arrival_stop")
        public Arrival arrival;

        @Key("departure_stop")
        public Departure departure;

        @Key("headsign")
        public String headsign;

        @Key("line")
        public Line line;
    }

    public static class Arrival {
        @Key("name")
        public String name;
    }

    public static class Departure {
        @Key("name")
        public String name;
    }

    public static class Line {
        @Key("short_name")
        public String route;
    }
}
