package in.tvac.akshayejh.photoblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;


    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    static ProfilePostRecyclerAdapter blogRecyclerAdapter;


    private String user_id;

    private boolean isChanged = false;

    private EditText setupName, setupMobile, setupJob, setupAdress;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Bitmap compressedImageFile;


    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    private String IS_ADMIN = "false";

    private static Pattern usrNamePtrn = Pattern.compile("^[a-z0-9_-]{6,14}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle(this.getResources().getString(R.string.profile_toolbar));


        blog_list = new ArrayList<>();
        blog_list_view = findViewById(R.id.profile_post_list);

        blogRecyclerAdapter = new ProfilePostRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);


        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setupAdress = findViewById(R.id.setup_adress);
        setupJob = findViewById(R.id.setup_job);
        setupMobile = findViewById(R.id.setup_mobile);
        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progress);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();


        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        loadMorePost();

                    }

                }
            });


            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blog_list.clear();

                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                if (isFirstPageFirstLoad) {

                                    if (user_id.equals(blogPost.getUser_id())) {
                                        blog_list.add(blogPost);
                                    }

                                } else {
                                    if (user_id.equals(blogPost.getUser_id())) {

                                        blog_list.add(0, blogPost);
                                    }

                                }


                                blogRecyclerAdapter.notifyDataSetChanged();

                            }
                        }

                        isFirstPageFirstLoad = false;

                    }

                }

            });

        }


        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String adress = task.getResult().getString("adress");
                        String mobile = task.getResult().getString("mobile");
                        String job = task.getResult().getString("job");

                        mainImageURI = Uri.parse(image);

                        setupName.setText(name);
                        setupAdress.setText(adress);
                        setupJob.setText(job);
                        setupMobile.setText(mobile);


                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setupName.getText().toString();
                final String user_adress = setupAdress.getText().toString();
                final String user_job = setupJob.getText().toString();
                final String user_mobile = setupMobile.getText().toString();

//final String user_name = setupName.getText().toString();
//                final String user_adress = setupAdress.getText().toString();
//                final String user_job = setupJob.getText().toString();
//                final String user_mobile = setupMobile.getText().toString();

//                !TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(user_adress) && !TextUtils.isEmpty(user_job) && !TextUtils.isEmpty(user_mobile) && mainImageURI != null

//                dataValidation();


                if (!isValidString(user_name)) {
                    setupName.setError("Please Enter Valid Name..");

                }else if (!isValidString(user_adress)) {
                    setupAdress.setError("Please Enter Valid Adress");

                }
                else if (!isValidMobile(user_mobile)){
                    setupMobile.setError("Please Enter Valid mobile number..");
                }
                else if (!isValidString(user_job)){
                    setupJob.setError("Please Enter Valid job");
                }
                else if (mainImageURI == null){
                    Toast.makeText(SetupActivity.this, "Insert Your Profile Image", Toast.LENGTH_SHORT).show();
                }
                    else
                 {

                    setupProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        File newImageFile = new File(mainImageURI.getPath());
                        try {

                            compressedImageFile = new Compressor(SetupActivity.this)
                                    .setMaxHeight(125)
                                    .setMaxWidth(125)
                                    .setQuality(50)
                                    .compressToBitmap(newImageFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask image_path = storageReference.child("profile_images").child(user_id + ".jpg").putBytes(thumbData);

                        image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, user_name, user_adress, user_job, user_mobile);

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();

                                    setupProgress.setVisibility(View.INVISIBLE);

                                }
                            }
                        });


                    }
                    else {
                        storeFirestore(null, user_name, user_adress, user_job, user_mobile);

                    }

                }

            }


        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }

        });


    }

    public void dataValidation() {

        //Validation of Data to register
        if (setupName.getText().toString().length() == 0)
            setupName.setError("Field cannot be left empty!");

        else if (setupJob.getText().toString().length() == 0)
            setupJob.setError("Field cannot be left empty!");

        else if (setupMobile.getText().toString().length() == 0)
            setupMobile.setError("Field cannot be left empty!");

        else if (setupAdress.getText().toString().length() == 0)
            setupAdress.setError("Field cannot be left empty!");

        else if (!isValidMobile(setupMobile.getText().toString()))
            setupMobile.setError("Please enter valid mobile number");

        else if (!isValidString(setupAdress.getText().toString()))
            setupAdress.setError("Please enter valid adress");

        else if (!isValidString(setupJob.getText().toString()))
            setupJob.setError("Please enter valid jop");

        else if (!isValidString(setupName.getText().toString()))
            setupName.setError("Please enter valid name");

//        else{
//            //handle Register here
//            registerUser = new User(
//                    useNameET.getText().toString(),
//                    userEmailET.getText().toString(),
//                    userPhoneET.getText().toString(),
//                    userAdressET.getText().toString(),
//                    userJopET.getText().toString(),
//                    userPasswordET.getText().toString(),
//                    "false");
//
//            registerUser(registerUser);
//        }
//
//
    }


//    Validation function

    private boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() < 6 || phone.length() > 13) {
                // if(phone.length() != 10) {
                check = false;
            } else {
                check = true;
            }
        } else {
            check = false;
        }
        return check;
    }


    public static boolean isValidString(String input) {
        try {
            int i = Integer.parseInt(input);
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
    }


    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name, String user_adress, String user_job, String user_mobile) {

        Uri download_uri;

        if (task != null) {

            download_uri = task.getResult().getDownloadUrl();

        } else {

            download_uri = mainImageURI;

        }


        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("adress", user_adress);
        userMap.put("job", user_job);
        userMap.put("mobile", user_mobile);
        userMap.put("isadmin", IS_ADMIN);
        userMap.put("image", download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(SetupActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);

            }
        });


    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }


    public void loadMorePost() {

        if (firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                if (user_id.equals(blogPost.getUser_id())) {

                                    blog_list.add(blogPost);
                                }
                                blogRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                }
            });

        }

    }


}
