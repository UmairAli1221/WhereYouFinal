package com.uberclone.whereyou.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uberclone.whereyou.Activities.CreatGroup;
import com.uberclone.whereyou.Model.CustomInfoWindowGoogleMap;
import com.uberclone.whereyou.Model.InfoWindowData;
import com.uberclone.whereyou.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View mView;
    PlaceAutocompleteFragment autocompleteFragment;
    private InfoWindowData info;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mView != null) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null)
                parent.removeView(mView);
        }
        try {
            mView = inflater.inflate(R.layout.fragment_home, container, false);

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SupportMapFragment fragment = new SupportMapFragment();
        transaction.add(R.id.map, fragment);
        transaction.commit();

        //
        info = new InfoWindowData();

        fragment.getMapAsync(this);

        autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng location = place.getLatLng();
                String locationName = place.getName().toString();
                Search(location, locationName);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
        autocompleteFragment.setHint("Search here");

       return mView;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
    private void Search(LatLng location, final String locationName) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location)
                .title(locationName)
                .snippet("S")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location));

        info.setLocation(locationName);
        info.setCity(locationName);

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(getContext());
        mMap.setInfoWindowAdapter(customInfoWindow);

        Marker m=mMap.addMarker(markerOptions);
        m.setTag(info);
        m.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        final double lat = location.latitude;
        final double log = location.longitude;
        /*mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent newIntent = new Intent(getActivity(), CreatGroup.class);
                newIntent.putExtra("placeName", locationName);
                Bundle b = new Bundle();
                b.putDouble("Lat", lat);
                b.putDouble("Log", log);
                newIntent.putExtras(b);
                startActivity(newIntent);
            }
        });*/

    }
}
