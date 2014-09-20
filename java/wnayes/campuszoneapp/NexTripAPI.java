package wnayes.campuszoneapp;

import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class NexTripAPI {
    public static ArrayList<Departure> getDepartures(int stopId) throws SocketTimeoutException, ConnectTimeoutException {
        String url = "http://svc.metrotransit.org/NexTrip/" + Integer.toString(stopId) + "?format=json";
        RestClient client = new RestClient(url);

        // Prevent the search from stalling indefinitely.
        client.SetTimeout(10000, 10000);

        try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (SocketTimeoutException sce) {
            Log.e("NexTripAPI.getDepartures", "Socket connection timeout.");
            throw sce;
        } catch (ConnectTimeoutException cte) {
            Log.e("NexTripAPI.getDepartures", "HTTP Connection timeout.");
            throw cte;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.d("NexTripAPI.getDepartures", (client.getResponse() == null) ? "" : client.getResponse());

        // Parse the stop times and grab the latest one.
        try {
            JSONArray stopsArray = new JSONArray(client.getResponse());
            ArrayList<Departure> departures = Departure.parseList(stopsArray);
            return departures;
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
