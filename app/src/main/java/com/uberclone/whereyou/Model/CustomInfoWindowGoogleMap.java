package com.uberclone.whereyou.Model;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.uberclone.whereyou.R;

/**
 * Created by Umair Ali on 1/9/2018.
 */

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.popup, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(350,220));

        TextView tv_location_name = view.findViewById(R.id.location_name);
        TextView tv_location_city = view.findViewById(R.id.location_city_name);
        ImageView iv_review = view.findViewById(R.id.btn_review_location);
        ImageView iv_existing = view.findViewById(R.id.btn_see_existing);

        //tv_location_name.setText(marker.getTitle());
       // tv_location_city.setText(marker.getSnippet());

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        tv_location_name.setText(infoWindowData.getLocation());
        tv_location_city.setText(infoWindowData.getCity());

        return view;
    }
}