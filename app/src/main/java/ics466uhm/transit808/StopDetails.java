package ics466uhm.transit808;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class StopDetails extends ActionBarActivity {
    private ListAdapter adapter;
    private ListView view;

    // Navigation drawer fields.
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private String stopID;
    private BusStop stop;


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
        mActivityTitle = getTitle().toString();
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
                        intent = new Intent(StopDetails.this, BusStopSearchActivity.class);
                        break;
                    case 2:
                        intent = new Intent(StopDetails.this, Trips.class);
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

        /**
        Intent intent = getIntent();
        TextView title = (TextView) findViewById(R.id.stop_title);
        title.append(intent.getStringExtra(BusStopSearchActivity.STREET_NAME_MESSAGE));
        RetrieveFeed feed = new RetrieveFeed();
        feed.execute(prepareURL(intent.getStringExtra(BusStopSearchActivity.BUS_STOP_ID)));
        stopID = intent.getStringExtra(BusStopSearchActivity.BUS_STOP_ID);
        **/
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            stop = bundle.getParcelable("stop");
            TextView title = (TextView) findViewById(R.id.stop_title);
            title.append(stop.getStreetName());
            RetrieveFeed feed = new RetrieveFeed();
            feed.execute(prepareURL(stop.getStopID()));
            changeButtonState();
        }
    }

    public String prepareURL(String busStopID) {
        String result = getResources().getString(R.string.hea_url).replace("API_key",
                getResources().getString(R.string.hea_api)).replace("stop_ID", busStopID);
        Log.i("HEA_URL", result);
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
    class RetrieveFeed extends AsyncTask<String, Integer, String> {
        static final String PARENT_ELEMENT = "arrival";
        static final String KEY_ID = "id";
        static final String KEY_ROUTE = "route";
        static final String KEY_HEADSIGN = "headsign";
        static final String KEY_STOPTIME = "stopTime";
        private ArrayList<HashMap<String, String>> arrivals = new ArrayList<HashMap<String, String>>();

        public RetrieveFeed() {
        }

        @Override
        protected String doInBackground(String... params) {
            String xml = getXMLFromURL(params[0]);
            Document doc = this.getDomElement(xml);
            NodeList nodeList = doc.getElementsByTagName(PARENT_ELEMENT);

            for (int i = 0; i < nodeList.getLength(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                Element e = (Element) nodeList.item(i);
                map.put(KEY_ROUTE, getValue(e, KEY_ROUTE));
                map.put(KEY_HEADSIGN, getValue(e, KEY_HEADSIGN));
                map.put(KEY_STOPTIME, getValue(e, KEY_STOPTIME));
                arrivals.add(map);
            }

            return "SUCCESS";
        }

        public String getXMLFromURL(String url) {
            String xml = url;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
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
        protected void onPostExecute(String result) {
            adapter = new SimpleAdapter(StopDetails.this, arrivals, R.layout.stop_item,
                    new String[] {KEY_ROUTE, KEY_HEADSIGN, KEY_STOPTIME},
                    new int[] {R.id.route, R.id.headsign, R.id.arrivalTime});
            ListView list = (ListView) findViewById(R.id.stop_times);
            list.setAdapter(adapter);
        }
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
