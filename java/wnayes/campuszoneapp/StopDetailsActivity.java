package wnayes.campuszoneapp;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import wnayes.campuszoneapp.Departure.Direction;


public class StopDetailsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections.
     */
    DepartureListPagerAdapter mSectionsPagerAdapter;

    /** The {@link ViewPager} that will host the section contents. */
    ViewPager mViewPager;

    LRTStation station;
    ArrayList<Departure> westboundDepartures;
    ArrayList<Departure> eastboundDepartures;
    Direction startingDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);

        // Read station information from intent.
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            this.station = intent.getParcelableExtra("LRTStation");
            this.startingDirection =
                 Direction.fromValue(intent.getIntExtra("StartingDirection", Direction.WESTBOUND.getValue()));
            if (intent.hasExtra("WestboundDepartures") && intent.hasExtra("EastboundDepartures")) {
                this.westboundDepartures = intent.getParcelableArrayListExtra("WestboundDepartures");
                this.eastboundDepartures = intent.getParcelableArrayListExtra("EastboundDepartures");
            } else {
                new refreshStationTimeAPICaller().execute(this.station);
            }

        } else {
            this.startingDirection = Direction.fromValue(savedInstanceState.getInt("StartingDirection"));
            this.station = savedInstanceState.getParcelable("LRTStation");
            westboundDepartures = savedInstanceState.getParcelableArrayList("WestboundDepartures");
            eastboundDepartures = savedInstanceState.getParcelableArrayList("EastboundDepartures");
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(this.station.getStationName() + " Departures");
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new DepartureListPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Switch to eastbound tab based on the stop number.
        if (this.startingDirection == Direction.EASTBOUND)
            mViewPager.setCurrentItem(1);

        mViewPager.refreshDrawableState();

        PagerTabStrip tabStrip = (PagerTabStrip)findViewById(R.id.pagerTabStrip);
        tabStrip.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.altColorBackground));
        tabStrip.setTabIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.altColorForeground));
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("StartingDirection", this.startingDirection.getValue());
        savedInstanceState.putParcelable("LRTStation", this.station);
        savedInstanceState.putParcelableArrayList("WestboundDepartures", westboundDepartures);
        savedInstanceState.putParcelableArrayList("EastboundDepartures", eastboundDepartures);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stop_details, menu);
        return true;
    }

    /** A {@link FragmentPagerAdapter} that returns a fragment corresponding to a direction. */
    public class DepartureListPagerAdapter extends FragmentPagerAdapter {

        public DepartureListPagerAdapter(FragmentManager fm) {
            super(fm);
            map = new SparseArray<DepartureListFragment>(2);
        }

        private SparseArray<DepartureListFragment> map;

        public DepartureListFragment getFragment(int position) {
            return map.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            DepartureListFragment newFragment;
            if (position == 0)
                newFragment = DepartureListFragment.newInstance(station, Direction.WESTBOUND, westboundDepartures);
            else
                newFragment = DepartureListFragment.newInstance(station, Direction.EASTBOUND, eastboundDepartures);
            map.put(position, newFragment);
            return newFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            map.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.westbound).toUpperCase(l);
                case 1:
                    return getString(R.string.eastbound).toUpperCase(l);
            }
            return null;
        }
    }

    private class refreshStationTimeAPICaller extends AsyncTask<LRTStation, Void, ArrayList<ArrayList<Departure>>> {
        protected void onPreExecute() {

        }

        protected ArrayList<ArrayList<Departure>> doInBackground(LRTStation... stations) {
            ArrayList<ArrayList<Departure>> departures = new ArrayList<>(2);
            try {
                departures.add(NexTripAPI.getDepartures(stations[0], Departure.Direction.WESTBOUND));
                departures.add(NexTripAPI.getDepartures(stations[0], Departure.Direction.EASTBOUND));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "NexTripAPI threw!");
                e.printStackTrace();
            }

            return departures;
        }

        protected void onPostExecute(ArrayList<ArrayList<Departure>> departures) {
            if (departures.size() != 2 || departures.get(0) == null || departures.get(1) == null) {
                Log.e(this.getClass().getSimpleName(), "Could not download W/E departures");
                Toast.makeText(getApplicationContext(), "Could not download station info. Check internet?",
                               Toast.LENGTH_SHORT).show();
                return;
            }

            westboundDepartures = departures.get(0);
            eastboundDepartures = departures.get(1);

            DepartureListPagerAdapter adapter = (DepartureListPagerAdapter)mViewPager.getAdapter();
            adapter.getFragment(0).updateDepartureList(westboundDepartures);
            adapter.getFragment(1).updateDepartureList(eastboundDepartures);
        }
    }
}
