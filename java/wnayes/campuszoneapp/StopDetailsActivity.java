package wnayes.campuszoneapp;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class StopDetailsActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    int mStartingStopId;
    int mWestboundStopId;
    int mEastboundStopId;
    String stationName;
    ArrayList<Departure> westboundDepartures;
    ArrayList<Departure> eastboundDepartures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);

        // Read intent data
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mWestboundStopId = intent.getIntExtra("WestboundStopId", 0);
            mEastboundStopId = intent.getIntExtra("EastboundStopId", 0);
            mStartingStopId = intent.getIntExtra("StartingStopId", 0);
            stationName = intent.getStringExtra("StationName");
            westboundDepartures = intent.getParcelableArrayListExtra("WestboundDepartures");
            eastboundDepartures = intent.getParcelableArrayListExtra("EastboundDepartures");
        } else {
            mWestboundStopId = savedInstanceState.getInt("WestboundStopId");
            mEastboundStopId = savedInstanceState.getInt("EastboundStopId");
            mStartingStopId = savedInstanceState.getInt("StartingStopId");
            stationName = savedInstanceState.getString("StationName");
            westboundDepartures = savedInstanceState.getParcelableArrayList("WestboundDepartures");
            eastboundDepartures = savedInstanceState.getParcelableArrayList("EastboundDepartures");
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(stationName + " Departures");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                                      .setText(mSectionsPagerAdapter.getPageTitle(i))
                                      .setTabListener(this));
        }

        // Switch to eastbound tab based on the stop number.
        if (mStartingStopId == mEastboundStopId)
            mViewPager.setCurrentItem(1);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("WestboundStopId", mWestboundStopId);
        savedInstanceState.putInt("EastboundStopId", mEastboundStopId);
        savedInstanceState.putInt("StartingStopId", mStartingStopId);
        savedInstanceState.putString("StationName", stationName);
        savedInstanceState.putParcelableArrayList("WestboundDepartures", westboundDepartures);
        savedInstanceState.putParcelableArrayList("EastboundDepartures", eastboundDepartures);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stop_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return DepartureListFragment.newInstance(mWestboundStopId, westboundDepartures);
            return DepartureListFragment.newInstance(mEastboundStopId, eastboundDepartures);
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



}
