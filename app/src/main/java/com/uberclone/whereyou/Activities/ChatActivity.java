package com.uberclone.whereyou.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uberclone.whereyou.Adapter.MessageAdapter;
import com.uberclone.whereyou.MainActivity;
import com.uberclone.whereyou.Model.Messages;
import com.uberclone.whereyou.R;
import com.uberclone.whereyou.Util.FileUtils;
import com.uberclone.whereyou.Util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@RuntimePermissions
public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mGroupTitle;
    private TextView mLastSeenView;
    private TextView mTyingText;
    private CircleImageView mGroupImage;
    private String mCurrentUserId;
    private EmojiconEditText mChatMessageView;
    View rootView;
    private ProgressDialog mprogressDialog;
    private EmojIconActions emojIcon;
    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerView mMessagesList;
    String picturePath;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private DatabaseReference mUserRef, mMembersDatabase;
    private FirebaseAuth mAuth;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    //keep track of camera capture intent
    static final int CAMERA_CAPTURE = 1;
    //keep track of cropping intent
    final int PIC_CROP = 3;
    //keep track of gallery intent
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Bitmap bitmap;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private StorageReference imageStorage;
    LinearLayout mContainerImg;
    private String userName;
    private DatabaseReference mNotificationDatabase;
    ImageView mAddImg, mEmojiBtn, mDeleteImg, mPreviewImg;
    TextView mSendMessage;
    String rewiveCreater, userChoosenTask;
    private CameraModule cameraModule;
    Intent takePicture;
    Uri file;
    private File output = null;
    private final static int MY_PERMISSIONS_REQUEST_CAMERA = 9;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Roboto-Regular.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_chat);
        //Toolbar Init View
        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //Firebase Init Views
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mChatUser = getIntent().getStringExtra("from_group_id");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("ReviewNotifications");
        imageStorage = FirebaseStorage.getInstance().getReference();
        mMembersDatabase = FirebaseDatabase.getInstance().getReference().child("Reviews").child(mChatUser).child("group_members");
        mMembersDatabase.keepSynced(true);
        //Inflate Chat App Bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_app_bar, null);
        actionBar.setCustomView(action_bar_view);
        //App Bar Init Views
        mGroupTitle = (TextView) findViewById(R.id.groupName);
        mPreviewImg = (CircleImageView) findViewById(R.id.groupImage);

        //Init Views
        mAddImg = (ImageView) findViewById(R.id.addImg);
        mContainerImg = (LinearLayout) findViewById(R.id.container_img);
        mDeleteImg = (ImageView) findViewById(R.id.deleteImg);
        mEmojiBtn = (ImageView) findViewById(R.id.emojiBtn);
        mContainerImg.setVisibility(View.GONE);
        mPreviewImg = (ImageView) findViewById(R.id.previewImg);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_layout_swip);

        //Progress Dialoge
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setTitle("Sending...");
        mprogressDialog.setMessage("Please Wait.....");
        mprogressDialog.setCanceledOnTouchOutside(false);

        //Emoje SetUp
        rootView = findViewById(R.id.root_view);
        mSendMessage = (TextView) findViewById(R.id.sendMessage);
        mChatMessageView = (EmojiconEditText) findViewById(R.id.TextMessage);
        emojIcon = new EmojIconActions(this, rootView, mChatMessageView, mEmojiBtn);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                mChatMessageView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }

            @Override
            public void onKeyboardClose() {
            }
        });

        //Select Image
        mAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        //Send Message
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });
        //Refresh Messages
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
        loadMessages();

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.listView);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);


        mMessagesList.setAdapter(mAdapter);
        mGroupTitle.setText(userName);

        mRootRef.child("Reviews").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("reviewname") && dataSnapshot.hasChild("uid_created_by")) {
                    String groupName = dataSnapshot.child("reviewname").getValue().toString();
                    userName = groupName;
                    mGroupTitle.setText(groupName);
                    rewiveCreater = dataSnapshot.child("uid_created_by").getValue().toString();
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void SelectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(ChatActivity.this);
                if (options[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        ChatActivityPermissionsDispatcher.selectPicWithCheck(ChatActivity.this);

                } else if (options[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Gallery";
                    if (result) {
//                        galleryIntent();
                        com.esafirm.imagepicker.features.ImagePicker.create(ChatActivity.this)
                                .folderMode(true)
                                .showCamera(true)
                                .theme(R.style.AppTheme)
                                .single()
                                .returnAfterFirst(true).start(SELECT_FILE);
                    }
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @NeedsPermission({ Manifest.permission.CAMERA})
    public void selectPic() {
        takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        output = new File(dir, "CameraContentDemo.jpeg");
        uploadImage(String.valueOf(output));
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

        startActivityForResult(takePicture, REQUEST_CAMERA);
    }
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
//
                if (data != null) {
                    ArrayList<Image> images = (ArrayList<Image>) com.esafirm.imagepicker.features.ImagePicker.getImages(data);
                    String filePath = images.get(0).getPath();
                    String mimeType = FileUtils.getMimeType(new File(filePath));
//                        if (mimeType.contains("video")) uploadThumbnail(filePath);
                    uploadImage(filePath);
                }
            } else if (requestCode == REQUEST_CAMERA) {
                if (data != null) {

//
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    i.setDataAndType(Uri.fromFile(output), "image/jpeg");
                    startActivity(i);

                    finish();
                }
            }

//

        }

    }
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void uploadImage(String filePath) {
        File fileToUpload = new File(filePath);
//        fileToUpload = ImageCompressorUtil.compressImage(this, fileToUpload);
        filePath = fileToUpload.getAbsolutePath();
        sendImageMessage(filePath);
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_read_ext_storage_rationale, request);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_read_ext_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.permission_read_ext_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }
    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForDownloadingFile(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_write_ext_storage_rationale, request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForDownloadingFile() {
        Toast.makeText(this, R.string.permission_write_ext_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForDownloadingFile() {
        Toast.makeText(this, R.string.permission_write_ext_storage_never_askagain, Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    public void sendImageMessage(String file) {
        final String current_user_ref = "Chats/" + mChatUser + "/messages";
        // final String chat_user_ref = "Chat/" + mChatUser + "/" + mCurrentUserId + "/messages";

        DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatUser).child("messages").push();
        final String push_id = user_message_push.getKey();

        //  mprogressDialog2.dismiss();
        final File fileToUpload = new File(file);
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();
        final StorageReference filapath = imageStorage.child("message_images").child(push_id + ".jpg");
        filapath.putFile(Uri.fromFile(fileToUpload)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    filapath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            Map messageMap = new HashMap();
                            messageMap.put("message", uri.toString());
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            // messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            // mChatMessageView.setText("");

                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if (databaseError != null) {

                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        //  mprogressDialog2.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Not Sent..", Toast.LENGTH_SHORT).show();

                                    } else {
                                        checkAndCopy("/" + getString(R.string.app_name) + "/", fileToUpload, push_id);
                                        final Map notification = new HashMap<>();
                                        notification.put("from", mCurrentUserId);
                                        notification.put("from_group_id", mChatUser);
                                        notification.put("type", "Single_message");
                                        notification.put("message", "Photo!");
                                        if (!rewiveCreater.equals(mCurrentUserId)) {
                                            mNotificationDatabase.child(rewiveCreater).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("CHAT_LOG", "Done");
                                                }
                                            });
                                        }
                                    }

                                }
                            });

                        }
                    });
                } else {
                    // mprogressDialog2.dismiss();
                    Toast.makeText(ChatActivity.this, "Error In Uploading Image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkAndCopy(String directory, File source, String push_id) {
        //Create and copy file content
        File file = new File(Environment.getExternalStorageDirectory(), directory);
        boolean dirExists = file.exists();
        if (!dirExists)
            dirExists = file.mkdirs();
        if (dirExists) {
            try {
                file = new File(Environment.getExternalStorageDirectory() + directory, push_id + ".jpg");
                boolean fileExists = file.exists();
                if (!fileExists)
                    fileExists = file.createNewFile();
                if (fileExists && file.length() == 0) {
                    FileUtils.copyFile(source, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Chats").child(mChatUser).child("messages");
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);


                String messageKey = dataSnapshot.getKey();
                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {

                    mLastKey = messageKey;
                }


                mAdapter.notifyDataSetChanged();
                // mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        final String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Chats/" + mChatUser + "/messages";
            //String chat_user_ref = "Chat/" + mGroupId + "/" + mCurrentUserId+"/messages";

            DatabaseReference user_message_push = mRootRef.child("Chats").child(mChatUser).child("messages").push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            //messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    } else {
                        mRootRef.child("Reviews").child(mChatUser).child("last_message").setValue(message);
                        mRootRef.child("Reviews").child(mChatUser).child("name").setValue(userName);
                        mRootRef.child("Reviews").child(mChatUser).child("last_message_time").setValue(ServerValue.TIMESTAMP);
                        final Map notification = new HashMap<>();
                        notification.put("from", mCurrentUserId);
                        notification.put("fromGroup", userName);
                        notification.put("from_group_id", mChatUser);
                        notification.put("type", "Single_message");
                        notification.put("message", message);
                        if (!rewiveCreater.equals(mCurrentUserId)) {
                            mNotificationDatabase.child(rewiveCreater).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("CHAT_LOG", "Done");
                                }
                            });
                        }
                    }
                }
            });

        }


    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("Chats").child(mChatUser).child("messages");
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;
                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChatActivity.this, MainActivity.class));
    }
}


