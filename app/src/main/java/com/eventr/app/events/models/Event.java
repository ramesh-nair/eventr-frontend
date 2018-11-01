package com.eventr.app.events.models;

import android.util.Log;

import com.eventr.app.events.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Suraj on 24/08/16.
 */
public class Event implements Serializable {
    private String name, id, description, picUrl;
    private String location;
    private Date date;
    private String rsvpStatus;
    private String pleaseNote;

    public Event() {
    }

    public  Event(JSONObject event, String rsvpStatus, boolean alt) {
        setDetails(event, rsvpStatus, alt);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return this.picUrl;
    }

    public String getDateAndMonth() {
        return Utils.getDateAndMonth(this.date);
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocationString() {
        try {
            JSONObject placeObj = new JSONObject(this.location);
            String name = placeObj.getString("name");
            JSONObject ct = placeObj.getJSONObject("city");
            String city = ct.getString("name");
            JSONObject countryJS = placeObj.getJSONObject("country");
            String country = countryJS.getString("name");
            return name + ", " + city + ", " + country;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDetails(JSONObject event, String rsvpStatus, boolean alt) {
        this.rsvpStatus = rsvpStatus;
        try {
            this.id = event.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.name = event.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (event.has("promoter")) {
            JSONObject promoter = null;
            try {
                promoter = event.getJSONObject("promoter");
                if(promoter.has("description")) {
                    try {
                        this.description = event.getJSONObject("promoter").getString("description");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            if (alt) {
                this.picUrl = event.getString("coverPicture");
            } else {
                if (event.has("images")) {
                    JSONArray images = event.getJSONArray("images");
                    if(images.length()>0){
                        JSONObject img = images.getJSONObject(0);
                        this.picUrl = img.getString("url");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (alt) {
                this.location = event.getJSONObject("venue").toString();
            } else {
                if (event.has("_embedded")) {
                    JSONObject embedded = event.getJSONObject("_embedded");
                    if (embedded.has("venues")) {
                        JSONArray venues = embedded.getJSONArray("venues");
                        if(venues.length()>0){
                            this.location = venues.get(0).toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.pleaseNote = event.getString("pleaseNote");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (alt) {
                this.date = Utils.getDateFromString(event.getString("startTime"));
            } else {
                if (event.has("dates")) {
                    JSONObject dates = event.getJSONObject("dates");
                    if (dates.has("start")) {
                        JSONObject start = dates.getJSONObject("start");
                        date = Utils.getDateFromString(start.getString("dateTime"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTimeString() {
        return Utils.getTimeString(this.date);
    }

    public String getRsvpStatus() {
        return this.rsvpStatus;
    }



    public void setRsvpStatus(String status) {
        this.rsvpStatus = status;
    }

    public String getPleaseNote() {
        return this.pleaseNote;
    }

    public void setPleaseNote(String pleaseNote) {
        this.pleaseNote = pleaseNote;
    }
}
