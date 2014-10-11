package wnayes.campuszoneapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CampusZoneActivity extends ActionBarActivity
       implements CampusZoneStopOverview.StopOverviewFragmentListener,
                  SwipeRefreshLayout.OnRefreshListener {

    private CampusZoneStopOverview stopOverviewFragment;

    // SharedPreferences file names.
    private static String SETTINGS_STOP_DATA = "SERIALIZED_STOPS";
    private static String SETTINGS_GENERAL = "SETTINGS_GENERAL";

    // Initial refresh should happen only once the actionbar is ready and the
    // child fragment has loaded its view.
    private boolean refreshCheck_onCreateOptionsMenu = false;
    private boolean refreshCheck_FragmentonCreateView = false;
    private boolean refreshCheck_done = false;

    /** Storage for the latest sets of departure info, accessed via stop id. */
    private HashMap<Integer, ArrayList<Departure>> departureInfo;

    // 56043 WB westbound
    // 56042 EB westbound
    // 56041 SV westbound
    // 56001 WB eastbound
    // 56002 EB eastbound
    // 56003 SV eastbound
    public static final Integer campusZoneStops[] = { 56043, 56042, 56041, 56001, 56002, 56003 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_zone);

        // Show the last refresh time text.
        SharedPreferences settings = getSharedPreferences(SETTINGS_GENERAL, 0);
        if (settings.contains("LastRefresh")) {
            Date lastRefresh = new Date(settings.getLong("LastRefresh", 0));
            if (lastRefresh.getTime() > 0)
                this.updateRefreshLabel(lastRefresh);
        }

        // Setup the activity when new. See onRestoreInstanceState for configuration changes.
        if (savedInstanceState == null) {
            stopOverviewFragment = new CampusZoneStopOverview();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, stopOverviewFragment)
                    .commit();

            this.departureInfo = new HashMap<Integer, ArrayList<Departure>>(6);
        } else {
            stopOverviewFragment = (CampusZoneStopOverview)getSupportFragmentManager().getFragment(savedInstanceState, "stopOverviewFragment");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor settings = getSharedPreferences(SETTINGS_STOP_DATA, 0).edit();
        settings.clear();
        Iterator it = this.departureInfo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            settings.putString(pairs.getKey().toString(), Departure.createList((ArrayList<Departure>)pairs.getValue()).toString());
            it.remove();
        }
        settings.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.departureInfo == null)
            this.departureInfo = new HashMap<Integer, ArrayList<Departure>>(6);

        SharedPreferences settings = getSharedPreferences(SETTINGS_STOP_DATA, 0);
        for (int stopId : CampusZoneActivity.campusZoneStops) {
            if (!settings.contains(Integer.toString(stopId)))
                continue;
            String jsonArray = settings.getString(Integer.toString(stopId), "[]");
            JSONArray arr;
            try {
                arr = new JSONArray(jsonArray);
            } catch (JSONException jse) {
                jse.printStackTrace();
                continue;
            }
            ArrayList<Departure> list = Departure.parseList(arr);
            this.departureInfo.put(stopId, list);
            stopOverviewFragment.updateStopTime(stopId, list.get(0), null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the fragment's instance
        getSupportFragmentManager().putFragment(savedInstanceState, "stopOverviewFragment", stopOverviewFragment);

        // Save initial refresh state info
        savedInstanceState.putBoolean("refreshCheck_done", this.refreshCheck_done);
        savedInstanceState.putBoolean("refreshCheck_FragmentonCreateView", this.refreshCheck_FragmentonCreateView);
        savedInstanceState.putBoolean("refreshCheck_onCreateOptionsMenu", this.refreshCheck_onCreateOptionsMenu);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the fragment's instance
        // stopOverviewFragment = (CampusZoneStopOverview)getSupportFragmentManager().getFragment(savedInstanceState, "stopOverviewFragment");

        // Restore refresh check info
        this.refreshCheck_done = savedInstanceState.getBoolean("refreshCheck_done");
        this.refreshCheck_FragmentonCreateView = savedInstanceState.getBoolean("refreshCheck_FragmentonCreateView");
        this.refreshCheck_onCreateOptionsMenu = savedInstanceState.getBoolean("refreshCheck_onCreateOptionsMenu");
    }

    private Menu _menu;
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu == null)
            menu = new MenuBuilder(getApplicationContext());

        menu.clear();
        getMenuInflater().inflate(R.menu.campus_zone, menu);

        // Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;

        if (!this.refreshCheck_done) {
            this.refreshCheck_onCreateOptionsMenu = true;
            this.refreshCheck();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh_event) {
            this.onRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateRefreshLabel(Date refreshDate) {
        Calendar now = Calendar.getInstance();
        Calendar refreshCal = Calendar.getInstance();
        refreshCal.setTime(refreshDate);
        String label = getString(R.string.last_refresh);
        if (now.get(Calendar.DAY_OF_YEAR) != refreshCal.get(Calendar.DAY_OF_YEAR) &&
            now.get(Calendar.YEAR) != refreshCal.get(Calendar.YEAR)) {
            label += " on " + new SimpleDateFormat("EEE, MMM d, ''yy").format(refreshDate);
        } else {
            label += " at " + new SimpleDateFormat("h:mma").format(refreshDate);
        }
        getSupportActionBar().setSubtitle(label);
    }

    @Override
    public void onCreatedView() {
        this.refreshCheck_FragmentonCreateView = true;
        this.refreshCheck();
    }

    public void onStopSelected(int stopId) {
        // Prevent launching activity before data has loaded.
        if (departureInfo.size() < campusZoneStops.length)
            return;

        // Launch activity showing detailed stop information
        Intent intent = new Intent(this, StopDetailsActivity.class);
        intent.putExtra("StartingDirection", Departure.Direction.EASTBOUND.getValue());
        switch (stopId) {
            case 56043:
                intent.putExtra("StartingDirection", Departure.Direction.WESTBOUND.getValue());
            case 56001:
                intent.putExtra("LRTStation", LRTStation.greenLineStations.get(4));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56043));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56001));
                break;
            case 56042:
                intent.putExtra("StartingDirection", Departure.Direction.WESTBOUND.getValue());
            case 56002:
                intent.putExtra("LRTStation", LRTStation.greenLineStations.get(5));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56042));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56002));
                break;
            case 56041:
                intent.putExtra("StartingDirection", Departure.Direction.WESTBOUND.getValue());
            case 56003:
                intent.putExtra("LRTStation", LRTStation.greenLineStations.get(6));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56041));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56003));
                break;
            default:
                Log.e("CampusZoneActivity.onStopSelected", "Bad stop ID selected");
        }
        startActivity(intent);
    }

    private void refreshCheck() {
        if (this.refreshCheck_done)
            return;
        if (this.refreshCheck_FragmentonCreateView && this.refreshCheck_onCreateOptionsMenu) {
            this.onRefresh();
            this.refreshCheck_done = true;
        }
    }

    @Override
    public void onRefresh() {
        Log.d("CampusZoneActivity.onRefresh", "Refreshing stop times.");
        ((SwipeRefreshLayout)stopOverviewFragment.getView().findViewById(R.id.swipe_container))
                .setRefreshing(true);
        new refreshStopTimeAPICaller().execute(campusZoneStops);
    }

    private class refreshStopTimeAPICaller extends AsyncTask<Integer, Void, ArrayList<Integer>> {
        /** Quick access to the refresh button in the actionbar. */
        MenuItem refreshItem;
        protected void onPreExecute() {
            // Establish progress UI changes.
            if (_menu != null) {
                refreshItem = _menu.findItem(R.id.menu_refresh_event);
                if (refreshItem != null)
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_refresh_progress);
            }
        }

        protected ArrayList<Integer> doInBackground(Integer... ids) {
            ArrayList<Integer> stopIds = new ArrayList<Integer>(ids.length);
            for (Integer stopId : ids) {
                try {
                    ArrayList<Departure> departures = NexTripAPI.getDepartures(stopId);
                    if (departures != null && departures.size() > 0) {
                        departureInfo.put(stopId, departures);
                        stopIds.add(stopId);
                    }
                } catch (Exception e) {
                    Log.e("refreshStopTimeAPICaller", "NexTripAPI threw!");
                    e.printStackTrace();
                }
            }

            return stopIds;
        }

        protected void onPostExecute(ArrayList<Integer> stopIds) {
            // Remove progress UI.
            if (refreshItem != null)
                MenuItemCompat.setActionView(refreshItem, null);
            refreshItem = null;

            if (stopOverviewFragment.isAdded()) {
                ((SwipeRefreshLayout)stopOverviewFragment.getView().findViewById(R.id.swipe_container))
                        .setRefreshing(false);
            }

            // If the event can't be found, no UI refresh should occur.
            if (stopIds.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "Could not download stop info. Check internet?",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Remember the last time a refresh occurred.
            SharedPreferences.Editor settings = getSharedPreferences(SETTINGS_GENERAL, 0).edit();
            settings.putLong("LastRefresh", new Date().getTime());
            updateRefreshLabel(new Date());
            settings.apply();

            // Invalidate the options menu to account for any needed visibility changes.
            if (!ActivityCompat.invalidateOptionsMenu(CampusZoneActivity.this))
                CampusZoneActivity.this.onCreateOptionsMenu(_menu);

            if (stopOverviewFragment.isAdded()) {
                for (Integer stopId : stopIds) {
                    if (departureInfo.containsKey(stopId))
                        stopOverviewFragment.updateStopTime(stopId, departureInfo.get(stopId).get(0), null);
                }
            }
        }
    }
}
