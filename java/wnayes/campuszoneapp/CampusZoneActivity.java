package wnayes.campuszoneapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CampusZoneActivity extends ActionBarActivity
       implements CampusZoneStopOverview.StopOverviewFragmentListener {

    private CampusZoneStopOverview stopOverviewFragment;

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

        this.departureInfo = new HashMap<Integer, ArrayList<Departure>>(6);

        // Setup the activity when new. See onRestoreInstanceState for configuration changes.
        if (savedInstanceState == null) {
            stopOverviewFragment = new CampusZoneStopOverview();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, stopOverviewFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.departureInfo == null || this.departureInfo.size() < campusZoneStops.length)
            this.refreshStopTimes();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the fragment's instance
        getSupportFragmentManager().putFragment(savedInstanceState, "stopOverviewFragment", stopOverviewFragment);

        // Save stop information.
        Iterator it = this.departureInfo.entrySet().iterator();
        ArrayList<Integer> stopIds = new ArrayList<Integer>();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            stopIds.add((Integer)pairs.getKey());
            savedInstanceState.putParcelableArrayList(pairs.getKey().toString(), (ArrayList<Departure>)pairs.getValue());
            it.remove();
        }
        savedInstanceState.putIntegerArrayList("stopIds", stopIds);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the fragment's instance
        stopOverviewFragment = (CampusZoneStopOverview)getSupportFragmentManager().getFragment(savedInstanceState, "stopOverviewFragment");

        // Restore departureInfo
        this.departureInfo = new HashMap<Integer, ArrayList<Departure>>(6);
        ArrayList<Integer> stopIds = savedInstanceState.getIntegerArrayList("stopIds");
        for (Integer stopId : stopIds) {
            this.departureInfo.put(stopId, savedInstanceState.<Departure>getParcelableArrayList(stopId.toString()));
        }
    }

    private Menu _menu;
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu == null)
            menu = new MenuBuilder(getApplicationContext());

        menu.clear();
        getMenuInflater().inflate(R.menu.campus_zone, menu);

        // Keep a reference to the menu for later uses (refresh indicator change).
        this._menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh_event) {
            this.refreshStopTimes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshStopTimes() {
        Log.d("CampusZoneActivity.refreshStopTimes", "Refreshing stop times.");
        new refreshStopTimeAPICaller().execute(campusZoneStops);
    }

    public void onStopSelected(int stopId) {
        // Prevent launching activity before data has loaded.
        if (departureInfo.size() < campusZoneStops.length)
            return;

        // Launch activity showing detailed stop information
        Intent intent = new Intent(this, StopDetailsActivity.class);
        switch (stopId) {
            case 56043:
            case 56001:
                intent.putExtra("StationName", getResources().getString(R.string.station_name_westbank));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56043));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56001));
                break;
            case 56042:
            case 56002:
                intent.putExtra("StationName", getResources().getString(R.string.station_name_eastbank));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56042));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56002));
                break;
            case 56041:
            case 56003:
                intent.putExtra("StationName", getResources().getString(R.string.station_name_stadiumvillage));
                intent.putParcelableArrayListExtra("WestboundDepartures", departureInfo.get(56041));
                intent.putParcelableArrayListExtra("EastboundDepartures", departureInfo.get(56003));
                break;
            default:
                Log.e("CampusZoneActivity.onStopSelected", "Bad stop ID selected");
        }
        startActivity(intent);
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
                stopIds.add(stopId);
                try {
                    ArrayList<Departure> departures = NexTripAPI.getDepartures(stopId);
                    if (departures != null)
                        departureInfo.put(stopId, departures);
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

            // If the event can't be found, no UI refresh should occur.
            if (stopIds.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "Could not download stop info. Check internet?",
                        Toast.LENGTH_SHORT).show();
                return;
            }

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
