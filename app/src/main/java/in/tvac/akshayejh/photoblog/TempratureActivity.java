package in.tvac.akshayejh.photoblog;

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

import java.util.ArrayList;

public class TempratureActivity extends AppCompatActivity {


    RecyclerView recyclerView ;
    TempratureAdapter tempratureAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temprature);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Temprature Table");

        recyclerView = findViewById(R.id.temp_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));




        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("server-POST");



        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                DataModel value ;
                ArrayList<DataModel> dataModels = new ArrayList<>();

                for(DataSnapshot d : dataSnapshot.getChildren()){
                    String[] data = d.child("TEMP").getValue().toString().split(" ");
                    value = new DataModel(data[0],data[1]+data[2]);
                    dataModels.add(value);
                }

                tempratureAdapter = new TempratureAdapter(dataModels,R.layout.temp_row,TempratureActivity.this);
                recyclerView.setAdapter(tempratureAdapter);


                }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


    }
}
