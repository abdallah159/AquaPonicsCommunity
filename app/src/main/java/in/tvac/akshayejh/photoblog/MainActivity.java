package in.tvac.akshayejh.photoblog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;

import okhttp3.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import in.tvac.akshayejh.photoblog.utilities.GPSTracker;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences ShredRef;

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;

    private TextView profileName;

    int min, max;

    private CircleImageView profileImage;

    private FloatingActionButton addPostBtn;

    private BottomNavigationView mainbottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private AdminFragment adminFragment;

    private Uri mainImageURI = null;
    private StorageReference storageReference;

    String IS_ADMIN;

    public static boolean isAppRunning;
    JSONObject message;
    JSONObject messageInfo;

    private final String PUSH_URL = "https://fcm.googleapis.com/fcm/send";

    private String USER_TOKEN;


    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("server-POST");


    DrawerLayout drawer;


    TextView cityNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ShredRef = this.getSharedPreferences("CurrentUser", this.MODE_PRIVATE);
        IS_ADMIN = ShredRef.getString("isAdmin", "false");




        //Toast.makeText(this, ""+min+max, Toast.LENGTH_SHORT).show();


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        if(IS_ADMIN.equals("true")) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "1";
            String channel2 = "2";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        "Channel 1", NotificationManager.IMPORTANCE_HIGH);

                notificationChannel.setDescription("This is BNT");
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(notificationChannel);

            }


            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            USER_TOKEN = refreshedToken;
            Log.e("+++++++", "Refreshed token: " + refreshedToken);

            message = new JSONObject();
            messageInfo = new JSONObject();

            try {
                messageInfo.put("title", "Temprature Alarm!");
                messageInfo.put("message", "Water Tempreture is Up Normal!!");
                messageInfo.put("image-url", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Achtung.svg/180px-Achtung.svg.png");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                message.put("to", USER_TOKEN);
                message.put("data", messageInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            AndroidNetworking.initialize(getApplicationContext());

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .addNetworkInterceptor(new StethoInterceptor()).build();

            AndroidNetworking.initialize(getApplicationContext(), okHttpClient);


            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    DataModel value;
                    ArrayList<DataModel> dataModels = new ArrayList<>();
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        String[] data = d.child("TEMP").getValue().toString().split(" ");
//                    Last edit of the data activity
                        value = new DataModel(data[0], null);
//                    Toast.makeText(TempratureActivity.this, ""+time[0], Toast.LENGTH_SHORT).show();
                        dataModels.add(value);
                    }

                    min = Integer.parseInt(ShredRef.getString("Min", "24"));
                    max = Integer.parseInt(ShredRef.getString("Max", "27"));

                    if (Float.parseFloat(dataModels.get(dataModels.size() - 1).getTEMP()) >= max || Float.parseFloat(dataModels.get(dataModels.size() - 1).getTEMP()) <= min) {
                        //Push Notification from here..
                        AndroidNetworking.post(PUSH_URL)
                                .addJSONObjectBody(message)
                                .addHeaders("Authorization", "key=AAAAOTvZvqw:APA91bF0CYY19WBxUwOaePlNsFzGf3wjjxhlM8D-EjqWQXkFIiLmXSwFgLUPd8XQt1uTxCDGPbPC8Q-GfuLFSlkEvHTWLk1hYnBrCgfgHSJOZ5Ah8T2rId_0VAi5WHD9FKJav0XwNiBl")
                                .addHeaders("Content-Type", "application/json")
                                .setPriority(Priority.MEDIUM)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                           // Toast.makeText(MainActivity.this, "l:" + response.getString("success").toString(), Toast.LENGTH_SHORT);
                                            Log.e("++++",response.getString("success").toString());

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError anError) {

                                    }
                                });


                    }


                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });

        }


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                homeFragment = new HomeFragment();
                notificationFragment = new NotificationFragment();
                accountFragment = new AccountFragment();
                adminFragment = new AdminFragment();
                initializeFragment();

                switch (id) {
                    case R.id.profile:
                        drawer.closeDrawers();
                        Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                        startActivity(settingsIntent);
                        return true;

                    case R.id.home:
                        drawer.closeDrawers();
                        addPostBtn.setVisibility(View.VISIBLE);
                        replaceFragment(homeFragment, currentFragment);
                        return true;

                    case R.id.notifiaction:
                        drawer.closeDrawers();
                        addPostBtn.setVisibility(View.GONE);
                        replaceFragment(notificationFragment, currentFragment);
                        return true;


                    case R.id.usesystem:
                        drawer.closeDrawers();
                        if (IS_ADMIN.equals("true")) {
                            addPostBtn.setVisibility(View.GONE);
                            replaceFragment(adminFragment, currentFragment);
                            return true;
                        } else {
                            addPostBtn.setVisibility(View.GONE);
                            replaceFragment(accountFragment, currentFragment);
                            return true;
                        }


                    case R.id.about:
                        drawer.closeDrawers();
                        Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(aboutIntent);
                        return true;

                    case R.id.logout:
                        logOut();
                        return true;


                    default:
                        return true;
                }
            }
        });


        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        View header = navigationView.getHeaderView(0);

        String user_id = mAuth.getCurrentUser().getUid();


        profileImage = header.findViewById(R.id.imageView);


        profileName = header.findViewById(R.id.profilename);


        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");


                        Log.e("++++++++", name + image);
                        mainImageURI = Uri.parse(image);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);

                        profileName.setText(name);


                    }

                } else {

                    String error = task.getException().getMessage();
                   // Toast.makeText(MainActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }

            }
        });
        if (mAuth.getCurrentUser() != null) {

            mainbottomNav = findViewById(R.id.mainBottomNav);

            // FRAGMENTS
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            adminFragment = new AdminFragment();

            initializeFragment();

            mainbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                    if (IS_ADMIN.equals("true")) {

                        switch (item.getItemId()) {

                            case R.id.bottom_action_home:

                                replaceFragment(homeFragment, currentFragment);
                                addPostBtn.setVisibility(View.VISIBLE);
                                return true;

                            case R.id.bottom_action_account:

                                replaceFragment(adminFragment, currentFragment);
                                addPostBtn.setVisibility(View.GONE);
                                return true;

                            case R.id.bottom_action_notif:

                                replaceFragment(notificationFragment, currentFragment);
                                addPostBtn.setVisibility(View.GONE);
                                return true;

                            default:
                                return false;


                        }

                    } else {

                        switch (item.getItemId()) {

                            case R.id.bottom_action_home:

                                replaceFragment(homeFragment, currentFragment);
                                addPostBtn.setVisibility(View.VISIBLE);
                                return true;

                            case R.id.bottom_action_account:
                                addPostBtn.setVisibility(View.GONE);
                                replaceFragment(accountFragment, currentFragment);
                                return true;

                            case R.id.bottom_action_notif:
                                addPostBtn.setVisibility(View.GONE);
                                replaceFragment(notificationFragment, currentFragment);
                                return true;

                            default:
                                return false;


                        }


                    }

                }
            });


            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);

                }
            });

        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            sendToLogin();

        } else {

            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {

                        String errorMessage = task.getException().getMessage();
                       // Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


                    }

                }
            });

        }


    }


    private void logOut() {


        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }

    private void initializeFragment() {


        if (IS_ADMIN.equals("true")) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.add(R.id.main_container, homeFragment);
            fragmentTransaction.add(R.id.main_container, notificationFragment);
            fragmentTransaction.add(R.id.main_container, adminFragment);

            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(adminFragment);

            fragmentTransaction.commit();
        } else {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.add(R.id.main_container, homeFragment);
            fragmentTransaction.add(R.id.main_container, notificationFragment);
            fragmentTransaction.add(R.id.main_container, accountFragment);

            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(accountFragment);

            fragmentTransaction.commit();
        }


    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment) {

        if (IS_ADMIN.equals("true")) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fragment == homeFragment) {

                fragmentTransaction.hide(adminFragment);
                fragmentTransaction.hide(notificationFragment);

            }

            if (fragment == adminFragment) {

                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(notificationFragment);

            }

            if (fragment == notificationFragment) {

                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(adminFragment);

            }
            fragmentTransaction.show(fragment);

            //fragmentTransaction.replace(R.id.main_container, fragment);
            fragmentTransaction.commit();


        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fragment == homeFragment) {

                fragmentTransaction.hide(accountFragment);
                fragmentTransaction.hide(notificationFragment);

            }

            if (fragment == accountFragment) {

                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(notificationFragment);

            }

            if (fragment == notificationFragment) {

                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(accountFragment);

            }
            fragmentTransaction.show(fragment);

            //fragmentTransaction.replace(R.id.main_container, fragment);
            fragmentTransaction.commit();


        }

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@Nullable MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
    }


}
