package wnayes.campuszoneapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class NexTripAPI {
    public static ArrayList<Departure> getDepartures(LRTStation station, Departure.Direction direction) {
        String url = String.format("http://svc.metrotransit.org/NexTrip/902/%d/%s?format=json",
                     direction.getValue(), station.getAbbreviation());
        return getDepartures(url);
    }

    public static ArrayList<Departure> getDepartures(int stopId) {
        String url = String.format("http://svc.metrotransit.org/NexTrip/%d?format=json", stopId);
        return getDepartures(url);
    }

    private static ArrayList<Departure> getDepartures(String url) {
        String response;
        try {
            response = HttpRequest.get(url)
                .accept("application/json")
                .connectTimeout(10000)
                .readTimeout(10000)
                .body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.d("NexTrip getDepartures", url + ": " + ((response == null) ? "" : response));

        // Parse the stop times and grab the latest one.
        try {
            JSONArray stopsArray = new JSONArray(response);
            return Departure.parseList(stopsArray);
        } catch (JSONException e) {
            Log.e(NexTripAPI.class.toString(), "Error parsing stop array");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(NexTripAPI.class.toString(), "Other parsing error - likely networking issue.");
            e.printStackTrace();
        }

        return null;
    }
}
