package ics466uhm.transit808;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.maps.model.LatLng;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Trips extends ActionBarActivity {
    private AutoCompleteTextView from;
    private AutoCompleteTextView to;

    private List<String> resultList = new ArrayList<String>();
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String PLACES_API_AUTOCOMPLETION = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String PLACES_API_DIRECTIONS = "https://maps.googleapis.com/maps/api/directions/json?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_planner);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        from.setText("");
        to.setText("");

        from.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        to.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));

        DirectionsFetcher df = new DirectionsFetcher();
        df.execute();
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

        return super.onOptionsItemSelected(item);
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
                    if (constraint != null || constraint.length() == 0) {
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

                HttpRequest request = requestFactory.buildGetRequest(buildURL(s));
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

        private GenericUrl buildURL(String input) {
            // Build URL.
            GenericUrl url = new GenericUrl(PLACES_API_AUTOCOMPLETION);
            url.put("input", input);
            url.put("key", getResources().getString(R.string.google_browser_key));
            url.put("sensor", false);
            Log.i("URL", url.toString());
            return url;
        }
    }

    private class DirectionsFetcher extends AsyncTask<URL, Integer, String> {

        public DirectionsFetcher() {

        }

        private List<LatLng> latLngs = new ArrayList<LatLng>();
        @Override
        protected String doInBackground(URL... params) {
            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest httpRequest) throws IOException {
                        httpRequest.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                HttpRequest request = requestFactory.buildGetRequest(buildURL("Honolulu,HI", "Aiea,HI"));
                HttpResponse httpResponse = request.execute();
                DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
                List<Route> routes = directionsResult.routes;
                for (Route route : routes) {
                    List<Legs> legs = route.legs;
                    for (Legs leg : legs) {
                        System.out.println(legs.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {

        }
    }

    private GenericUrl buildURL(String origin, String destination) {
        // Build URL.
        GenericUrl url = new GenericUrl(PLACES_API_DIRECTIONS);
        url.put("origin", origin);
        url.put("destination", destination);
        url.put("sensor", false);
        url.put("mode", "transit");
        Log.i("URL_DIRECTIONS", url.toString());
        return url;
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

    public static class DirectionsResult {
        @Key("routes")
        public List<Route> routes;
    }

    public static class Route {
        @Key("legs")
        public List<Legs> legs;
    }

    public static class Legs {
        @Key("steps")
        public List<Step> steps;
    }

    public static class Mode {
        @Key("travel_mode")
        public String mode;
    }

    public static class Step {
        @Key("html_instructions")
        public String instructions;
    }
}
