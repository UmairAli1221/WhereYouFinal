package com.uberclone.whereyou.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.uberclone.whereyou.Activities.ChatActivity;
import com.uberclone.whereyou.Activities.LoginActivity;
import com.uberclone.whereyou.Model.Review;
import com.uberclone.whereyou.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends android.support.v4.app.Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private View mView;
    private DatabaseReference mUserRef;
    String mCurrentUser;
    private Map<String, Marker> markers;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private Marker mCurrent;
    PlaceAutocompleteFragment autocompleteFragment;
    private DatabaseReference mDatabase, mRootRef, mReviewDatabase, mReviewsLocations;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mGroup_Id;

    double ratingStar = 0.0;
    private SpotsDialog waitingdialog;
    TextView tv_location_name, tv_location_city;
    Button iv_review, iv_existing, btn_see_existing_disable, iv_review_disable;
    MaterialRatingBar materialRatingBar;
    EditText comment;
    Button submit;
    Context context;
    Marker markerTags;

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
            context = getContext();
        } catch (InflateException e) {
        }


        //Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Reviews");
        mDatabase.keepSynced(true);
        mReviewDatabase = FirebaseDatabase.getInstance().getReference().child("Reviews").push();
        mReviewDatabase.keepSynced(true);
        mReviewsLocations = FirebaseDatabase.getInstance().getReference().child("ReviewLocations");
        mReviewsLocations.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mGroup_Id = mReviewDatabase.getKey();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        //init Waitng SpotProgress
        waitingdialog = new SpotsDialog(getContext());
        return mView;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        creatLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request Runtime Permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                creatLocationRequest();
                displayLocation();
            }
        }
    }

    private void creatLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(getContext(), "This Device Is Not Supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //Toast.makeText(getContext(), "checked", Toast.LENGTH_SHORT).show();
            final double latitud = mLastLocation.getLatitude();
            final double logitude = mLastLocation.getLongitude();
            // SetMap(latitud, logitude);
            //mMap.clear();
               /* mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, logitude)).
                        title("You"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitud, logitude), 15.0f));*/
            GetReview(latitud, logitude);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                mMap.clear();
                final double lat = point.latitude;
                final double log = point.longitude;
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, log, 1);
                    String cityName = addresses.get(0).getAddressLine(0);
                    String stateName = addresses.get(0).getAddressLine(1);
                    String countryName = addresses.get(0).getAddressLine(2);
                    stopLocationUpdates();
                    GetReview(lat, log);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        setUpLocation();
        autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latLng = place.getLatLng();
                String locationName = place.getName().toString();
                stopLocationUpdates();
                GetReview(latLng.latitude, latLng.longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
        autocompleteFragment.setHint("Search here");

    }
    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser().getUid();
        } else {
            Intent startIntent = new Intent(getContext(), LoginActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
            getActivity().finish();
        }
    }

  /*  private void Search(final LatLng location, final String stateName, final String cityName, final String key) {
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location)
                .title(stateName)
                .snippet("S")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location));

        markerTags = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                final AlertDialog.Builder dialoge = new AlertDialog.Builder(getContext());
                final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View popup_layer = layoutInflater.inflate(R.layout.popup, null);
                tv_location_name = popup_layer.findViewById(R.id.location_name);
                tv_location_city = popup_layer.findViewById(R.id.location_city_name);
                iv_review = popup_layer.findViewById(R.id.btn_review_location);
                iv_existing = popup_layer.findViewById(R.id.btn_see_existing);
                btn_see_existing_disable = popup_layer.findViewById(R.id.btn_see_existing_disable);
                final String latstring = String.valueOf(location.latitude);
                final String longstring = String.valueOf(location.longitude);
                String lnglatString = latstring + "_" + longstring;
                mUserRef = FirebaseDatabase.getInstance().getReference().child("ReviewLocations");
                final GeoFire geoFire = new GeoFire(mUserRef);
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), 100);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, GeoLocation location) {
                        iv_existing.setVisibility(View.VISIBLE);
                        btn_see_existing_disable.setVisibility(View.GONE);
                        marker.setTag(key);
                        iv_existing.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getContext(), "ReviewID"+markerTags.getTag(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
                // btn_see_existing_disable.setVisibility(View.VISIBLE);
                //iv_existing.setVisibility(View.GONE);
                if (key != null) {
                    iv_existing.setVisibility(View.VISIBLE);
                    btn_see_existing_disable.setVisibility(View.GONE);
                    marker.setTag(key);
                    iv_existing.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "ReviewID" + key, Toast.LENGTH_SHORT).show();
                            Intent newIntent = new Intent(getContext(), ChatActivity.class);
                            newIntent.putExtra("user_id", key);
                            startActivity(newIntent);
                        }
                    });
                } else {
                    btn_see_existing_disable.setVisibility(View.VISIBLE);
                    iv_existing.setVisibility(View.GONE);
                }
                tv_location_city.setText(cityName);
                tv_location_name.setText(stateName);
                dialoge.setView(popup_layer);
                final AlertDialog b = dialoge.create();
                iv_review.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder dialoge2 = new AlertDialog.Builder(getContext());
                        //init view
                        b.dismiss();
                        View popu_review_layout = layoutInflater.inflate(R.layout.popup_review_screen, null);
                        tv_location_name = popu_review_layout.findViewById(R.id.location_name);
                        tv_location_city = popu_review_layout.findViewById(R.id.location_city_name);
                        materialRatingBar = popu_review_layout.findViewById(R.id.rating);
                        comment = popu_review_layout.findViewById(R.id.tv_comment);
                        submit = popu_review_layout.findViewById(R.id.btn_submit);
                        //
                        tv_location_city.setText(cityName);
                        tv_location_name.setText(stateName);
                        dialoge2.setView(popu_review_layout);
                        final AlertDialog b2 = dialoge2.create();
                        b2.show();
                        //Event Rating Bar
                        materialRatingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                            @Override
                            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                                ratingStar = rating;
                            }
                        });
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                creatReview(mGroup_Id, cityName, location);
                                b2.dismiss();
                            }
                        });
                    }
                });

                b.show();
                return true;
            }
        });
    }*/


    private void SetMap(final double latitud, final double logitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitud, logitude, 1);
            final String cityName = addresses.get(0).getAddressLine(0);
            final String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
            final String concateString = cityName + " " + stateName + "" + countryName;
            LatLng latLng = new LatLng(latitud, logitude);
            final AlertDialog.Builder d = new AlertDialog.Builder(getContext());
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View popup_layer = layoutInflater.inflate(R.layout.popup, null);
            tv_location_name = popup_layer.findViewById(R.id.location_name);
            tv_location_city = popup_layer.findViewById(R.id.location_city_name);
            iv_review = popup_layer.findViewById(R.id.btn_review_location);
            iv_existing = popup_layer.findViewById(R.id.btn_see_existing);
            btn_see_existing_disable = popup_layer.findViewById(R.id.btn_see_existing_disable);
            tv_location_city.setText(stateName);
            tv_location_name.setText(cityName);
            d.setView(popup_layer);
            final AlertDialog a1 = d.create();
            iv_review.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder d2 = new AlertDialog.Builder(getContext());
                    //init view
                    a1.dismiss();
                    View popu_review_layout = layoutInflater.inflate(R.layout.popup_review_screen, null);
                    tv_location_name = popu_review_layout.findViewById(R.id.location_name);
                    tv_location_city = popu_review_layout.findViewById(R.id.location_city_name);
                    materialRatingBar = popu_review_layout.findViewById(R.id.rating);
                    comment = popu_review_layout.findViewById(R.id.tv_comment);
                    submit = popu_review_layout.findViewById(R.id.btn_submit);
                    //
                    tv_location_city.setText(stateName);
                    tv_location_name.setText(cityName);
                    d2.setView(popu_review_layout);
                    final AlertDialog a2 = d2.create();
                    a2.show();
                    //Event Rating Bar
                    materialRatingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                        @Override
                        public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                            ratingStar = rating;
                        }
                    });
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LatLng latLng = new LatLng(latitud, logitude);
                            creatReview(mGroup_Id, cityName, latLng);
                            a2.dismiss();
                        }
                    });
                }
            });

            a1.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void GetReview(final double lat, final double log) {
        //  SetMap(lat, log);
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, log)).title("You")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location));

        markerTags = mMap.addMarker(markerOptions);
        markerTags.setTag("me");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, log), 15.0f));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                SetMap(lat, log);
                return false;
            }
        });
        mUserRef = FirebaseDatabase.getInstance().getReference().child("ReviewLocations");
        final GeoFire geoFire = new GeoFire(mUserRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, log), 100);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                // Toast.makeText(getContext(),""+key,Toast.LENGTH_SHORT).show();
                markerTags = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), 10.0f));
                markerTags.setTag(key);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        if (marker.getTag() != null && marker.getTag().toString() != "me") {
                            mDatabase.child(marker.getTag().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("reviewname")) {
                                        final String reviewName = dataSnapshot.child("reviewname").getValue().toString();
                                        final String latitude_lat = dataSnapshot.child("lat").getValue().toString();
                                        final String longitude_long = dataSnapshot.child("lng").getValue().toString();
                                        final String comm = dataSnapshot.child("comments").getValue().toString();
                                        //Dialoge Builder
                                        final AlertDialog.Builder dialoge = new AlertDialog.Builder(getContext());
                                        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                                        View popup_layer = layoutInflater.inflate(R.layout.popup, null);
                                        tv_location_name = popup_layer.findViewById(R.id.location_name);
                                        tv_location_city = popup_layer.findViewById(R.id.location_city_name);
                                        iv_review = popup_layer.findViewById(R.id.btn_review_location);
                                        iv_existing = popup_layer.findViewById(R.id.btn_see_existing);
                                        iv_review_disable = popup_layer.findViewById(R.id.btn_review_location_disable);
                                        btn_see_existing_disable = popup_layer.findViewById(R.id.btn_see_existing_disable);
                                        tv_location_city.setText(comm);
                                        tv_location_name.setText(reviewName);
                                        dialoge.setView(popup_layer);
                                        iv_existing.setVisibility(View.VISIBLE);
                                        btn_see_existing_disable.setVisibility(View.GONE);
                                        final AlertDialog b = dialoge.create();
                                        iv_existing.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                b.dismiss();
                                                String id = String.valueOf(marker.getTag());
                                                Intent newIntent = new Intent(getContext(), ChatActivity.class);
                                                newIntent.putExtra("from_user_id", id);
                                                startActivity(newIntent);

                                            }
                                        });
                                        iv_review.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                SetMap(location.latitude, location.longitude);
                                            }
                                        });
                                        b.show();

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else if (marker.getTag().toString() == "me") {
                            SetMap(lat, log);
                        }

                        return false;
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                Marker marker = markers.get(key);
                if (marker != null) {
                    marker.remove();
                    markers.remove(key);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Marker marker = markers.get(key);
                if (marker != null) {
                }
                //Toast.makeText(getContext(), "key moved but here", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                //Toast.makeText(getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void creatReview(final String mGroup_id, String cityName, final LatLng location) {
        String mRateStars = String.valueOf(ratingStar);
        String mComment = comment.getText().toString();
        if (TextUtils.isEmpty(mComment)) {
            comment.setError("Comment Cannot Be Empty");
            comment.requestFocus();
        } else if (ratingStar == 0.0) {
            comment.setError("Rate The Location");
            comment.requestFocus();
        } else {
            waitingdialog.show();
            Review review = new Review();
            review.setRates(mRateStars);
            review.setComments(mComment);
            review.setCreated_time(ServerValue.TIMESTAMP);
            review.setReview_id(mGroup_id);
            review.setReviewname(cityName);
            review.setLat(location.latitude);
            review.setLng(location.longitude);
            review.setUid_created_by(FirebaseAuth.getInstance().getCurrentUser().getUid());
            mDatabase.child(mGroup_id)
                    .setValue(review).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    GeoFire geoFire = new GeoFire(mReviewsLocations);
                    geoFire.setLocation(mGroup_id, new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            String latstring = String.valueOf(location.latitude);
                            String longstring = String.valueOf(location.longitude);
                            mDatabase.child(mGroup_id).child("lat_lng").setValue(latstring + "_" + longstring);
                            mRootRef.child(mCurrentUser).child("Reviews").child(mGroup_Id).child("null").setValue("");
                            waitingdialog.dismiss();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //Because We Need Runtime permisssions So overwrite the onRequestPermissionResult mthod
}
