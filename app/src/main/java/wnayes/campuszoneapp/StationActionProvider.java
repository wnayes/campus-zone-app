package wnayes.campuszoneapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.core.view.ActionProvider;

/** Creates a submenu of stations */
public class StationActionProvider extends ActionProvider
                                   implements MenuItem.OnMenuItemClickListener {

    public StationActionProvider(Context context) {
        super(context);
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        subMenu.clear();
        int i = 0;
        for (LRTStation station : LRTStation.greenLineStations) {
            subMenu.add(Menu.NONE, Menu.NONE, i++, station.getStationName())
                   .setIcon(R.drawable.station)
                   .setOnMenuItemClickListener(this);
        }
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Launch activity showing detailed stop information
        Intent intent = new Intent(getContext(), StopDetailsActivity.class);
        intent.putExtra("StartingDirection", Departure.Direction.WESTBOUND.getValue());
        intent.putExtra("LRTStation", LRTStation.greenLineStations.get(item.getOrder()));
        getContext().startActivity(intent);
        return true;
    }
}
