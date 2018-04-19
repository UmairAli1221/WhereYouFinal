package com.uberclone.whereyou.Adapter;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uberclone.whereyou.Activities.ImageViewerActivity;
import com.uberclone.whereyou.Model.Messages;
import com.uberclone.whereyou.R;
import com.uberclone.whereyou.Util.ResizableImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Umair Ali on 1/25/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private Animator mCurrentAnimator;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private final static int Outgoing_VIEW = 0;
    private final static int Ingoing_VIEW = 1;
    Context context;
    private int mShortAnimationDuration;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 786;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 1221;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutRes = 0;
        switch (viewType) {
            case Outgoing_VIEW:
                layoutRes = R.layout.message_single_layout;
                break;
            case Ingoing_VIEW:
                layoutRes = R.layout.message_single_layout2;
                break;
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, messangerName, mImageSize;
        public CircleImageView profileImage;
        public ImageView mMessageImage;
        public RelativeLayout mImageLayout;
        public TextView messagetime;
        public TextView sent;
        public ImageView mImagePlay;
        public ProgressBar progressBar3;


        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.left_message);
            profileImage = (CircleImageView) view.findViewById(R.id.left_fromUser);
            mMessageImage = (ImageView) view.findViewById(R.id.image);
            messagetime = (TextView) view.findViewById(R.id.left_timeAgo);
            messangerName = (TextView) view.findViewById(R.id.name);
            mImagePlay = (ImageView) view.findViewById(R.id.ImagePlay);
            progressBar3 = (ProgressBar) view.findViewById(R.id.progressBar3);
            mImageLayout=(RelativeLayout)view.findViewById(R.id.imageLayout);
            context = view.getContext();


        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i) {

        final Messages c = mMessageList.get(i);
        mAuth = FirebaseAuth.getInstance();
        String CurrentUser = mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();
        String message_type = c.getType();
        Long lastTime = c.getTime();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);
        if (i == Ingoing_VIEW) {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    viewHolder.messangerName.setText(name);

                    if (dataSnapshot.hasChild("profile_image")) {
                        final String image = dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.with(viewHolder.profileImage.getContext()).load(image)
                                .placeholder(R.drawable.ic_image_container).into(viewHolder.profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(viewHolder.profileImage.getContext()).load(R.drawable.image).into(viewHolder.profileImage);
                            }
                        });
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    viewHolder.messangerName.setText(name);
                    if (dataSnapshot.hasChild("profile_image")) {
                        final String image = dataSnapshot.child("profile_image").getValue().toString();


                        Picasso.with(viewHolder.profileImage.getContext()).load(image)
                                .placeholder(R.drawable.ic_image_container).into(viewHolder.profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(viewHolder.profileImage.getContext()).load(R.drawable.image).into(viewHolder.profileImage);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (message_type.equals("text")) {
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.mMessageImage.setVisibility(View.GONE);
            viewHolder.mImageLayout.setVisibility(View.GONE);
            viewHolder.messageText.setVisibility(View.VISIBLE);
        } else if (message_type.equals("image")) {
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.mImageLayout.setVisibility(View.VISIBLE);
            viewHolder.mMessageImage.setVisibility(View.VISIBLE);

            // Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
            // .placeholder(R.drawable.image).into(viewHolder.mMessageImage);
            context = viewHolder.itemView.getContext();
            final String imageName = getFileName(Uri.parse(c.getMessage()));
            if (!isPermissionGranted()) {
                try {
                    requestPermissionForReadExtertalStorage();

                    File direct = new File(Environment.getExternalStorageDirectory()
                            + "/WhereYou/" + imageName);

                    if (!direct.exists()) {
                        viewHolder.mImagePlay.setVisibility(View.VISIBLE);
                        viewHolder.mImagePlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//
//   Download(c.getMessage(), viewHolder.itemView.getContext(), imageName,viewHolder.progressBar3);
                                downloadFile(viewHolder.itemView.getContext(), c.getMessage(), imageName);
                            }
                        });
                    } else {
                        viewHolder.mImagePlay.setVisibility(View.GONE);
                        final Uri uri = Uri.fromFile(direct);
//                        Picasso.with(viewHolder.profileImage.getContext()).load(uri)
//                                .placeholder(R.drawable.ic_image_container).centerCrop()
//                                .fit().into(viewHolder.mMessageImage);
                        Glide.with(context).load(uri)
                                .apply(new RequestOptions().placeholder(R.drawable.ic_image_container).diskCacheStrategy(DiskCacheStrategy.ALL))
                                .into(viewHolder.mMessageImage);
                        viewHolder.mMessageImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(ImageViewerActivity.newInstance(context, String.valueOf(uri)));
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File direct = new File(Environment.getExternalStorageDirectory()
                        + "/WhereYou/" + imageName);

                if (!direct.exists()) {
                    viewHolder.mImagePlay.setVisibility(View.VISIBLE);
                    viewHolder.mImagePlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Download(c.getMessage(), viewHolder.itemView.getContext(), imageName, viewHolder.progressBar3);
                            downloadFile(viewHolder.itemView.getContext(), c.getMessage(), imageName);
                        }
                    });

                } else {
                    viewHolder.mImagePlay.setVisibility(View.GONE);
                    final Uri uri = Uri.fromFile(direct);
//                    Picasso.with(viewHolder.profileImage.getContext()).load(uri)
//                            .placeholder(R.drawable.ic_image_container).centerCrop()
//                            .fit().into(viewHolder.mMessageImage);
                    Glide.with(context).load(uri)
                .apply(new RequestOptions().placeholder(R.drawable.ic_image_container).diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(viewHolder.mMessageImage);
                    viewHolder.mMessageImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(ImageViewerActivity.newInstance(context, String.valueOf(uri)));
                        }
                    });

                }
            }
        }

        //-------------Timing Logic-----------//
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

        if (formattedDate.equals(dateFormat)) {
            viewHolder.messagetime.setText("Yesterday");
        } else if (today.equals(currentdate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
            Date dt;
            try {
                dt = sdf.parse(time);
                viewHolder.messagetime.setText(sdfs.format(dt));
                // <-- I got result here
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            viewHolder.messagetime.setText(today);
        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages c = mMessageList.get(position);
        mAuth = FirebaseAuth.getInstance();
        String CurrentUser = mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();
        if (from_user.equals(CurrentUser)) {
            return Ingoing_VIEW;
        } else {
            return Outgoing_VIEW;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void downloadFile(Context context, String url, String fileName) {
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(context.getString(R.string.app_name))
                .setDescription("Downloading " + fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(getDirectoryPath(context) + "/", fileName);
        mgr.enqueue(request);
    }

    private String getDirectoryPath(Context context) {
        return "/" + context.getString(R.string.app_name);
    }


    public boolean isWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void requestPermissionForWriteExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
