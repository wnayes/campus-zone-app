package wnayes.campuszoneapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/** Represents a train station */
public class LRTStation implements Parcelable {
    private String name;
    private String abbr;
    private int westboundStopId;
    private int eastboundStopId;

    public LRTStation(String name, String abbreviation, int westboundStopId, int eastboundStopId) {
        this.name = name;
        this.abbr = abbreviation;
        this.westboundStopId = westboundStopId;
        this.eastboundStopId = eastboundStopId;
    }

    public String getStationName() {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbr;
    }

    public int getWestboundStopId() {
        return this.westboundStopId;
    }

    public int getEastboundStopId() {
        return this.eastboundStopId;
    }

    public static List<LRTStation> greenLineStations = new ArrayList<LRTStation>() {
        {
            add(new LRTStation("Target Field", "TF1O", 0, 55997));
            add(new LRTStation("Warehouse/Hennepin Ave", "WARE", 0, 0));
            add(new LRTStation("Nicollet Mall", "5SNI", 0, 0));
            add(new LRTStation("Government Plaza", "GOVT", 0, 0));
            add(new LRTStation("Downtown East", "DTE", 0, 0));
            add(new LRTStation("West Bank", "WEBK", 56043, 56001));
            add(new LRTStation("East Bank", "EABK", 56042, 56002));
            add(new LRTStation("Stadium Village", "STVI", 56041, 56003));
            add(new LRTStation("Prospect Park", "PSPK", 0, 0));
            add(new LRTStation("Westgate", "WGAT", 0, 0));
            add(new LRTStation("Raymond Ave", "RAST", 0, 0));
            add(new LRTStation("Fairview Ave", "FAUN", 0, 0));
            add(new LRTStation("Snelling Ave", "SNUN", 0, 0));
            add(new LRTStation("Hamline Ave", "HMUN", 0, 0));
            add(new LRTStation("Lexington Pkwy", "LXUN", 0, 0));
            add(new LRTStation("Victoria St", "VIUN", 0, 0));
            add(new LRTStation("Dale St", "UNDA", 0, 0));
            add(new LRTStation("Western Ave", "WEUN", 0, 0));
            add(new LRTStation("Capitol/Rice St", "UNRI", 0, 0));
            add(new LRTStation("Robert St", "ROST", 0, 0));
            add(new LRTStation("10th St", "10CE", 0, 0));
            add(new LRTStation("Central", "CNST", 0, 0));
            add(new LRTStation("Union Depot", "UNDP", 0, 0));
        }
    };

    // Parcelable implementation
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.abbr);
        out.writeInt(this.westboundStopId);
        out.writeInt(this.eastboundStopId);
    }

    private LRTStation(Parcel in) {
        this.name = in.readString();
        this.abbr = in.readString();
        this.westboundStopId = in.readInt();
        this.eastboundStopId = in.readInt();
    }

    public static final Parcelable.Creator<LRTStation> CREATOR
            = new Parcelable.Creator<LRTStation>() {
        public LRTStation createFromParcel(Parcel in) {
            return new LRTStation(in);
        }
        public LRTStation[] newArray(int size) {
            return new LRTStation[size];
        }
    };
}
