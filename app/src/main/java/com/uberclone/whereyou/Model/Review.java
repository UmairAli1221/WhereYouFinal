package com.uberclone.whereyou.Model;

import java.util.Map;

/**
 * Created by Umair Ali on 1/22/2018.
 */

public class Review {
    private String rates, comments, uid_created_by, review_id, reviewname;
    private long created_time;
    private double lat, lng;

    public Review() {
    }

    public Review(String rates, String comments, String uid_created_by, String review_id, String reviewname, Long created_time, double lat, double lng) {
        this.rates = rates;
        this.comments = comments;
        this.uid_created_by = uid_created_by;
        this.review_id = review_id;
        this.reviewname = reviewname;
        this.created_time = created_time;
        this.lat = lat;
        this.lng = lng;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getUid_created_by() {
        return uid_created_by;
    }

    public void setUid_created_by(String uid_created_by) {
        this.uid_created_by = uid_created_by;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    public String getReviewname() {
        return reviewname;
    }

    public void setReviewname(String reviewname) {
        this.reviewname = reviewname;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}