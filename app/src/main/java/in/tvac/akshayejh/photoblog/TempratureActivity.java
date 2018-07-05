package in.tvac.akshayejh.photoblog;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;

public class TempratureActivity extends AppCompatActivity {


    RecyclerView recyclerView ;
    TempratureAdapter tempratureAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temprature);

        final ProgressDialog progressDialog ;

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Temprature Table");

        recyclerView = findViewById(R.id.temp_recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data..");
        progressDialog.show();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("server-POST");



        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                DataModel value ;
                ArrayList<DataModel> dataModels = new ArrayList<>();
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    String[] data = d.child("TEMP").getValue().toString().split(" ");
//                    Last edit of the data activity
                    String[] time = data[2].split("\\.");
                    value = new DataModel(data[0]+"ْ C","day: "+data[1]+"\n"+"time: "+time[0]);
//                    Toast.makeText(TempratureActivity.this, ""+time[0], Toast.LENGTH_SHORT).show();
                    dataModels.add(value);
                }

                tempratureAdapter = new TempratureAdapter(dataModels,R.layout.temp_row,TempratureActivity.this);
//                recyclerView.smoothScrollToPosition(dataModels.size());
                recyclerView.setAdapter(tempratureAdapter);
                progressDialog.dismiss();


                }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


    }
}
