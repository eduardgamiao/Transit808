package ics466uhm.transit808;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class StopDetails extends ActionBarActivity {
    private ListAdapter adapter;
    private ListView view;
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";

    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private String stopID;
    private BusStop stop;
    private RetrieveFeedArrival feed = new RetrieveFeedArrival();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);

        // Navigation drawer.
        mDrawerList = (ListView) findViewById(R.id.navList);
        addDrawerItems();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setupDrawer();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                Intent intent = null;
                switch(position) {
                    case 0:
                        intent = new Intent(StopDetails.this, MainActivity.class);
                        break;
                    case 1:
                        intent = new Intent(StopDetails.this, BusStopSearch.class);
                        break;
                    case 2:
                        intent = new Intent(StopDetails.this, TripPlanner.class);
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

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            stop = bundle.getParcelable("stop");
            TextView title = (TextView) findViewById(R.id.stop_title);
            title.setText(WordUtils.capitalizeFully(stop.getStreetName()));
            feed.execute(prepareArrivalURL(stop.getStopID()));
            changeButtonState();
        }

        Drawable drawable = getResources().getDrawable(R.drawable.ic_refresh_black_48dp);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.5), (int) (drawable.getIntrinsicHeight() * 0.5));
        Button button = (Button) findViewById(R.id.refresh);
        button.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            stop = bundle.getParcelable("stop");
            Log.i("RESUMING", stop.getStreetName());
            TextView title = (TextView) findViewById(R.id.stop_title);
            title.setText(stop.getStreetName());
            RetrieveFeedArrival feed = new RetrieveFeedArrival();
            feed.execute(prepareArrivalURL(stop.getStopID()));
            changeButtonState();
        }
    }

    private String prepareArrivalURL(String busStopID) {
        String result = getResources().getString(R.string.hea_arrival_url).replace("API_key",
                getResources().getString(R.string.hea_api)).replace("stop_ID", busStopID);
        //Log.i("HEA_URL", result);
        return result;
    }

    private  String prepareRouteURL(String busStopID) {
        String result = getResources().getString(R.string.hea_arrival_url).replace("API_key",
                getResources().getString(R.string.hea_api)).replace("stop_ID", busStopID);
        //Log.i("HEA_URL", result);
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stop_details, menu);
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

    /**
     * Async task to read XML from URL.
     * Code from www.androidhive.info/2011/11/android-xml-parsing-tutorial
     */
    class RetrieveFeedArrival extends AsyncTask<String, Integer, Boolean> {
        static final String PARENT_ELEMENT = "arrival";
        static final String KEY_ID = "id";
        static final String KEY_ROUTE = "route";
        static final String KEY_HEADSIGN = "headsign";
        static final String KEY_STOPTIME = "stopTime";
        static final String KEY_DATE = "date";
        static final String KEY_TEXT_TIME = "arrivalText";
        //private ArrayList<HashMap<String, String>> arrivals = new ArrayList<HashMap<String, String>>();
        private ArrayList<Bus> buses = new ArrayList<Bus>();

        public RetrieveFeedArrival() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String xml = getXMLFromURL(params[0]);
            Document doc = this.getDomElement(xml);
            if (doc != null) {
                NodeList nodeList = doc.getElementsByTagName(PARENT_ELEMENT);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    //HashMap<String, String> map = new HashMap<String, String>();
                    Element e = (Element) nodeList.item(i);
                    //map.put(KEY_ROUTE, getValue(e, KEY_ROUTE));
                    //map.put(KEY_HEADSIGN, getValue(e, KEY_HEADSIGN));
                    //map.put(KEY_STOPTIME, getValue(e, KEY_STOPTIME));
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    try {
                        Date date = formatter.parse(getValue(e, KEY_DATE) + " " + getValue(e, KEY_STOPTIME));
                        String output = "(Arriving "
                                + DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), 0).toString()
                                + ")";
                        Bus bus = new Bus(getValue(e, KEY_ROUTE), getValue(e, KEY_HEADSIGN), getValue(e, KEY_STOPTIME), output);
                        buses.add(bus);
                        //map.put(KEY_TEXT_TIME, output);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    //arrivals.add(map);
                }
                return true;
            }
            else {
                return false;
            }
        }

        public String getXMLFromURL(String url) {
            String xml = url;

            try {
                final HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
                HttpConnectionParams.setSoTimeout(httpParams, 30000);
                DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
                HttpPost httpPost = new HttpPost(url);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return xml;
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

        @Override
        protected void onPreExecute() {
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
            findViewById(R.id.stop_times).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                findViewById(R.id.loading).setVisibility(View.GONE);
                findViewById(R.id.stop_times).setVisibility(View.VISIBLE);

                BusAdapter adapter = new BusAdapter(getApplicationContext(), R.layout.stop_item, buses);
                ListView list = (ListView) findViewById(R.id.stop_times);
                list.setEmptyView(findViewById(R.id.emptyList));
                list.setAdapter(adapter);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // TODO
                    }
                });
            }
            else {
                showError();
            }
        }
    }

    private void showError() {
        findViewById(R.id.loading).setVisibility(View.GONE);
        findViewById(R.id.stop_times).setVisibility(View.GONE);
        TextView error = (TextView) findViewById(R.id.error);
        if (error != null) {
            error.setVisibility(View.VISIBLE);
        }
    }

    public void refresh(View view) {
        TextView error = (TextView) findViewById(R.id.error);
        error.setVisibility(View.GONE);
        feed = new RetrieveFeedArrival();
        feed.execute(prepareArrivalURL(stop.getStopID()));
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

    public void saveStop(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.addStop(stop);
        changeButtonState();
    }

    public void removeStop(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteStop(Integer.parseInt(stop.getStopID()));
        changeButtonState();
    }

    private void changeButtonState() {
        DatabaseHandler db = new DatabaseHandler(this);
        if (db.getStop(Integer.parseInt(stop.getStopID())) == null) {
            Button oldButton = (Button) findViewById(R.id.removeStop);
            oldButton.setVisibility(View.GONE);
            Button newButton = (Button) findViewById(R.id.addStop);
            newButton.setVisibility(View.VISIBLE);
        }
        else {
            Button oldButton = (Button) findViewById(R.id.removeStop);
            oldButton.setVisibility(View.VISIBLE);
            Button newButton = (Button) findViewById(R.id.addStop);
            newButton.setVisibility(View.GONE);
        }
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
