package wnayes.campuszoneapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * NexTrip Departure Information
 * https://svc.metrotransit.org/swagger/index.html
 *
 * ex: https://svc.metrotransit.org/NexTrip/56001?format=json
 */
public class Departure implements Parcelable {
    /** Transit vehicle departure prediction is using Actual(real-time) or scheduled information */
    public boolean Actual;

    /** Departure time if "Actual" field is false, Countdown in minutes if Actual is true */
    public String DepartureText;

    /** Predicted departure time	http://www.iso.org/iso/home/standards/iso8601.htm */
    public Date DepartureTime;

    /** Text describing transit vehicle destination as well as vehicle scroll (above front windshield) */
    public String Description;

    public enum Direction {
        SOUTHBOUND(2), EASTBOUND(0), WESTBOUND(1), NORTHBOUND(3), UNKNOWN(4);

        private int value;
        private Direction(int value) {
            this.value = value;
        }

        public static Direction fromString(String value) {
            String valueUpper = value.toUpperCase();
            switch (valueUpper) {
                case "SOUTHBOUND":
                case "SB":
                    return SOUTHBOUND;
                case "EASTBOUND":
                case "EB":
                    return EASTBOUND;
                case "WESTBOUND":
                case "WB":
                    return WESTBOUND;
                case "NORTHBOUND":
                case "NB":
                    return NORTHBOUND;
            }

            Log.e("Direction.fromString", "Unknown Direction string value");
            return UNKNOWN;
        }

        public static Direction fromValue(int value) {
            if (value == 1)
                return SOUTHBOUND;
            else if (value == 2)
                return EASTBOUND;
            else if (value == 3)
                return WESTBOUND;
            else if (value == 4)
                return NORTHBOUND;

            Log.e("Direction.fromString", "Unknown Direction string value");
            return UNKNOWN;
        }

        public int getValue() {
            return this.value;
        }
    }
    public Direction RouteDirection;

    public Departure(JSONObject departureInfo) throws JSONException {
        // Parse the JSON properties into their corresponding fields.
        this.Actual = departureInfo.getBoolean("actual");
        this.DepartureText = departureInfo.getString("departure_text");
        this.Description = departureInfo.getString("description");
        this.RouteDirection = Direction.fromString(departureInfo.getString("direction_text"));

        // Parse DepartureTime
        long epochMillis = departureInfo.getLong("departure_time") * 1000L;
        this.DepartureTime = new Date(epochMillis);
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("actual", this.Actual);
            obj.put("departure_text", this.DepartureText);
            obj.put("departure_time", this.DepartureTime.getTime() / 1000L);
            obj.put("description", this.Description);
            obj.put("direction_text", this.RouteDirection.name());
        } catch (JSONException jse) {
            Log.e("Departure.toJSON", "Could not serialize Departure into JSON!");
            jse.printStackTrace();
        }
        return obj;
    }

    public String getFormattedDepartureText() {
        if (this.Actual && this.DepartureText.equals("Due"))
            return this.DepartureText;
        return new SimpleDateFormat("h:mm").format(this.DepartureTime);
    }

    public static ArrayList<Departure> parseList(JSONArray list) {
        ArrayList<Departure> departures = new ArrayList<Departure>();
        try {
            for (int i = 0; i < list.length(); ++i)
                departures.add(new Departure(list.getJSONObject(i)));
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
        return departures;
    }

    public static JSONArray createList(ArrayList<Departure> departures) {
        JSONArray list = new JSONArray();
        for (Departure d : departures) {
            list.put(d.toJSON());
        }
        return list;
    }

    // Parcelable implementation
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (this.Actual ? 1 : 0));
        out.writeString(this.DepartureText);
        out.writeLong(this.DepartureTime.getTime() / 1000L);
        out.writeString(this.Description);
        out.writeInt(this.RouteDirection.value);
    }

    private Departure(Parcel in) {
        this.Actual = in.readByte() != 0;
        this.DepartureText = in.readString();
        this.DepartureTime = new Date(in.readLong() * 1000L);
        this.Description = in.readString();
        this.RouteDirection = Direction.fromValue(in.readInt());
    }

    public static final Parcelable.Creator<Departure> CREATOR
            = new Parcelable.Creator<Departure>() {
        public Departure createFromParcel(Parcel in) {
            return new Departure(in);
        }

        public Departure[] newArray(int size) {
            return new Departure[size];
        }
    };
}
