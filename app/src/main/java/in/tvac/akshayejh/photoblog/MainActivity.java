package in.tvac.akshayejh.photoblog;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import in.tvac.akshayejh.photoblog.utilities.GPSTracker;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences ShredRef;

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;

    private TextView profileName;

    private ImageView profileImage;

    private FloatingActionButton addPostBtn;

    private BottomNavigationView mainbottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;
    private AdminFragment adminFragment;

    private Uri mainImageURI = null;
    private StorageReference storageReference;

    String IS_ADMIN;


    DrawerLayout drawer;


    TextView cityNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * GPS Tracker I hope that it will run
         * */

         GPSTracker mGPS = new GPSTracker(this);

        cityNameTV = findViewById(R.id.location);
        if (mGPS.canGetLocation) {
            mGPS.getLocation();
            Toast.makeText(this, ""+mGPS.getLatitude()+mGPS.getLongitude(), Toast.LENGTH_SHORT).show();
         //   cityNameTV.setText("Lat" + mGPS.getLatitude() + "Lon" + mGPS.getLongitude());
        } else {
            cityNameTV.setText("Unabletofind");
        }

        ShredRef = this.getSharedPreferences("CurrentUser", this.MODE_PRIVATE);
        IS_ADMIN = ShredRef.getString("isAdmin", "false");


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Blog");


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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
                        if(IS_ADMIN.equals("true")){
                            addPostBtn.setVisibility(View.GONE);
                            replaceFragment(adminFragment, currentFragment);
                            return true;
                        }else {
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
                    Toast.makeText(MainActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

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
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


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
}
