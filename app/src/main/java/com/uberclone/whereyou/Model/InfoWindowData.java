package com.uberclone.whereyou.Model;

/**
 * Created by Umair Ali on 1/9/2018.
 */

public class InfoWindowData {
    String location,city;

    public InfoWindowData() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public InfoWindowData(String location, String city) {
        this.location = location;
        this.city = city;
    }
}
