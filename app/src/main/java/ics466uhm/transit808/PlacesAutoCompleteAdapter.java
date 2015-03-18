package ics466uhm.transit808;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ArrayAdapter for Google AutoComplete.
 * https://developers.google.com/places/training/autocomplete-android
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> resultList = new ArrayList<String>();
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String PLACES_API_AUTOCOMPLETION = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String PLACES_API_DIRECTIONS = "https://maps.googleapis.com/maps/api/directions/json?";
    private Context context;

    /**
     * Constructor.
     * @param context
     * @param textViewResourceId
     */
    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
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
        url.put("key", context.getResources().getString(R.string.google_browser_key));
        url.put("sensor", false);
        Log.i("URL", url.toString());
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

    private class DirectionsFetcher extends AsyncTask<URL, Integer, String> {
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

                HttpRequest request = requestFactory.buildGetRequest(buildURL("Aloha Stadium", "Pearlridge"));
                HttpResponse httpResponse = request.execute();
                DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
                String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
                latLngs = PolyUtil.decode(encodedPoints);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private GenericUrl buildURL(String origin, String destination) {
        // Build URL.
        GenericUrl url = new GenericUrl(PLACES_API_AUTOCOMPLETION);
        url.put("origin", origin);
        url.put("destination", destination);
        url.put("sensor", false);
        Log.i("URL_DIRECTIONS", url.toString());
        return url;
    }

    public static class DirectionsResult {
        @Key("routes")
        public List<Route> routes;
    }

    public static class Route {
        @Key("overview_polyline")
        public OverviewPolyLine overviewPolyLine;
    }

    public static class OverviewPolyLine {
        @Key("points")
        public String points;
    }
}
