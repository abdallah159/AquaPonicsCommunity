package in.tvac.akshayejh.photoblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;


public class AdminFragment extends Fragment {


    Button setAlarmBTN , toTempretureTable ;
    TextView temp ;

    Button graphBTN ;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("server-POST");




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_admin,
                container, false);


        temp = view.findViewById(R.id.tempTextView);

        graphBTN = view.findViewById(R.id.watch_graph);

        graphBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent graphIntent = new Intent(getContext(),GraphActivity.class);
                startActivity(graphIntent);

            }
        });


        setAlarmBTN = view.findViewById(R.id.set_alarm_btn);
        toTempretureTable = view.findViewById(R.id.watch_out_data_btn);


        setAlarmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent alarmIntent = new Intent(getContext(),AlarmActivity.class);
                startActivity(alarmIntent);
            }
        });

        toTempretureTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent alarmIntent = new Intent(getContext(),TempratureActivity.class);
                startActivity(alarmIntent);
            }
        });




        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                DataModel value ;
                ArrayList<DataModel> dataModels = new ArrayList<>();
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    String[] data = d.child("TEMP").getValue().toString().split(" ");
//                    Last edit of the data activity
                    value = new DataModel(data[0],null);
//                    Toast.makeText(TempratureActivity.this, ""+time[0], Toast.LENGTH_SHORT).show();
                    dataModels.add(value);
                }


                temp.setText(dataModels.get(dataModels.size()-1).getTEMP().toString()+"C");


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });



        return view;
    }


}
