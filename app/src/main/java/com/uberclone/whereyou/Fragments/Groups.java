package com.uberclone.whereyou.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uberclone.whereyou.Activities.ChatActivity;
import com.uberclone.whereyou.Model.Review;
import com.uberclone.whereyou.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class Groups extends Fragment {
    RecyclerView mMyChannels;
    ImageButton floatingActionButton;
    private DatabaseReference UsersDatabase, mRootRef, UserDatabase, database;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private LinearLayoutManager mLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_groups, container, false);
        mMyChannels = (RecyclerView) view.findViewById(R.id.mygroups);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMyChannels.setHasFixedSize(true);
        mMyChannels.setLayoutManager(mLayoutManager);

        //Firebase References
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Reviews");
        UsersDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        return view;
    }
    @Override
    public void onStart() {
        UserDatabase.child("online").setValue("true");
//        Query query=UsersDatabase.orderByChild("uid_created_by").equalTo(mCurrentUser);
//        Toast.makeText(getContext(), "Error In Uploading Image"+query, Toast.LENGTH_SHORT).show();
        super.onStart();
        FirebaseRecyclerAdapter<Review,AllGroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Review, AllGroupsViewHolder>(
                Review.class,
                R.layout.single_review_item,
               AllGroupsViewHolder.class,
                UsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final AllGroupsViewHolder viewHolder, Review model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Reviews").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String groupName;
                            if (dataSnapshot.hasChild("reviewname")){
                                groupName= dataSnapshot.child("reviewname").getValue().toString();
                                viewHolder.setName(groupName);
                            }
                            if(dataSnapshot.hasChild("comments")){
                                String des = dataSnapshot.child("comments").getValue().toString();
                                viewHolder.setStatus(des);
                            }
                            if (dataSnapshot.hasChild("created_time")){
                                String time=dataSnapshot.child("created_time").getValue().toString();
                                long lastTime = Long.parseLong(time);
                                viewHolder.setTime(lastTime);
                            }
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent(getContext(), ChatActivity.class);
                                    newIntent.putExtra("from_group_id", user_id);
                                    startActivity(newIntent);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };
        mMyChannels.setAdapter(firebaseRecyclerAdapter);
    }
    public static class AllGroupsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public AllGroupsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView usersNameView = (TextView) view.findViewById(R.id.location_name);
            usersNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView usersStatusView = (TextView) view.findViewById(R.id.location_comment);
            usersStatusView.setText(status);
        }


        public void setTime(long lastTime) {
            TextView lastMessagetime = (TextView) view.findViewById(R.id.location_time);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String formattedDate = formatter.format(lastTime);
            SimpleDateFormat todayformater = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String today = todayformater.format(lastTime);

            StringTokenizer tk = new StringTokenizer(formattedDate);
            String date = tk.nextToken();
            String time = tk.nextToken();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            dateFormat.format(cal.getTime());

            Calendar calender = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currentdate = df.format(calender.getTime());

            if (formattedDate.equals(dateFormat)){
                lastMessagetime.setText("Yesterday");
            }else if (today.equals(currentdate)){
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                Date dt;
                try {
                    dt = sdf.parse(time);
                    lastMessagetime.setText(sdfs.format(dt));
                    // <-- I got result here
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else {
                lastMessagetime.setText(today);
            }
        }
    }

}
