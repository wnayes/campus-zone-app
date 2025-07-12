package wnayes.campuszoneapp;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DepartureArrayAdapter extends ArrayAdapter<Departure> {
    private final ArrayList<Departure> departures;

    public DepartureArrayAdapter(Context context, int textViewResourceId, ArrayList<Departure> objects) {
        super(context, textViewResourceId, objects);
        this.departures = objects;
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.departure_list_item, null);
        }

        Departure departure = departures.get(position);
        if (departure == null)
            return view;

        ((TextView)view.findViewById(R.id.departure_time)).setText(departure.getFormattedDepartureText());
        if (!departure.DepartureText.equals("Due")) {
            ((TextView)view.findViewById(R.id.departure_time_ampm))
                .setText(new SimpleDateFormat("a", Locale.getDefault()).format(departure.DepartureTime));
        }
        ((TextView)view.findViewById(R.id.departure_time_type))
            .setText(departure.Actual ? R.string.predicted : R.string.scheduled);

        return view;
    }
}
