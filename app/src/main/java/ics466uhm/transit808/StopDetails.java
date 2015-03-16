package ics466uhm.transit808;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;


public class StopDetails extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);
        Intent intent = getIntent();
        TextView title = (TextView) findViewById(R.id.details_stop_title);
        String stopID = intent.getStringExtra(BusStopSearchActivity.BUS_STOP_ID);
        title.setText(intent.getStringExtra(BusStopSearchActivity.STREET_NAME_MESSAGE + "(#") + stopID + ")");
        RetrieveFeed feed = new RetrieveFeed();
        feed.execute(prepareURL(stopID));
    }

    public String prepareURL(String busStopID) {
        String result = getResources().getString(R.string.hea_url).replace("API_key",
                getResources().getString(R.string.hea_api)).replace("stop_ID", busStopID);
        Log.i("URL", result);
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

        return super.onOptionsItemSelected(item);
    }

    class RetrieveFeed extends AsyncTask<String, Integer, String> {
        public RetrieveFeed() {
        }

        @Override
        protected String doInBackground(String... params) {
            if (params != null || params.length > 0) {
                Log.i("PARAM CHECK", params.toString());
                getXMLFromURL(params[0]);
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
            Log.i("XML", xml);
            return xml;
        }
    }
}
