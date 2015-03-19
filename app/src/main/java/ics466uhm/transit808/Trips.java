package ics466uhm.transit808;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

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

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


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

    public void createTrip(View view) {
        EditText from = (EditText) findViewById(R.id.from);
        EditText to = (EditText) findViewById(R.id.to);

        Log.i("Create", from.getText() + " -> " + to.getText());

        DirectionsFetcher df = new DirectionsFetcher();

        String fromText = from.getText().toString();
        String toText = to.getText().toString();

        df.execute(fromText, toText);
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

    private class DirectionsFetcher extends AsyncTask<String, Integer, String> {
        static final String PARENT_ELEMENT = "step";
        static final String KEY_HTML_INSTRUCTION = "html_instructions";
        static final String KEY_HTML_DEPARTURE_STOP = "name";
        static final String KEY_HTML_ARRIVAL_STOP = "arrival_stop";
        private ArrayList<HashMap<String, String>> arrivals = new ArrayList<HashMap<String, String>>();

        public DirectionsFetcher() {

        }

        @Override
        protected String doInBackground(String... params) {
            //String xml = getXMLFromURL(params[0]);

            /**
            Document doc = this.getDomElement(xml);
            NodeList nodeList = doc.getElementsByTagName(PARENT_ELEMENT);

            for (int i = 0; i < nodeList.getLength(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                Element e = (Element) nodeList.item(i);
                map.put(KEY_HTML_INSTRUCTION, getValue(e, KEY_HTML_INSTRUCTION));
                map.put(KEY_HTML_DEPARTURE_STOP, getValue(e, KEY_HTML_DEPARTURE_STOP));
                arrivals.add(map);
            }
             **/

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
                int steps = directions.routes.get(0).step.get(0).instruction.size();
                for (int i = 0; i < steps; i++) {
                    Log.i("INSTRUCTION", directions.routes.get(0).step.get(0).instruction.get(i).instructions);
                    if (directions.routes.get(0).step.get(0).instruction.get(i).details != null) {
                        Log.i("DETAIL", directions.routes.get(0).step.get(0).instruction.get(i).details.departure.name);
                        Log.i("DETAIL", directions.routes.get(0).step.get(0).instruction.get(i).details.arrival.name);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "SUCCESS";
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            /**
            for (HashMap<String, String> arrival : arrivals) {
                Log.i("STEPS", arrival.get(KEY_HTML_INSTRUCTION) + " (" + arrival.get(KEY_HTML_DEPARTURE_STOP) + ")");
            }
             **/
        }

        public Document getDomElement(String xml) {
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is);

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return doc;
        }

        public String getXMLFromURL(String url) {
            String xml = url;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return xml;
        }

        public String getValue(Element item, String str) {
            NodeList nodeList = item.getElementsByTagName(str);
            return this.getElementValue(nodeList.item(0));
        }

        public final String getElementValue(Node element) {
            Node child;
            if (element != null) {
                if (element.hasChildNodes()) {
                    for (child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
                        if(child.getNodeType() == Node.TEXT_NODE) {
                            return child.getNodeValue();
                        }
                    }
                }
            }
            return "";
        }

    }

    private GenericUrl buildURLDirections(String origin, String destination) {
        // Build URL.
        GenericUrl url = new GenericUrl(PLACES_API_DIRECTIONS);
        url.put("origin", origin);
        url.put("destination", destination);
        url.put("sensor", false);
        url.put("mode", "transit");
        url.put("travel_mode", "bus");
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
        public List<Step> step;
    }

    public static class Step {
        @Key("steps")
        public List<Instruction> instruction;
    }

    public static class Instruction {
        @Key("html_instructions")
        public String instructions;

        @Key("transit_details")
        public Detail details;
    }

    public static class Detail {
        @Key("arrival_stop")
        public Arrival arrival;

        @Key("departure_stop")
        public Departure departure;
    }

    public static class Arrival {
        @Key("name")
        public String name;
    }

    public static class Departure {
        @Key("name")
        public String name;
    }
}
