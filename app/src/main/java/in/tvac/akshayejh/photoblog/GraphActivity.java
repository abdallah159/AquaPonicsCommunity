package in.tvac.akshayejh.photoblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    GraphView graphView;

    String[] time;

    ArrayList<DD> dataModels;
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(this.getResources().getString(R.string.graph_activity_toolbar));


        time = new String[0];


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("server-POST");


        graphView = findViewById(R.id.graph);

        graph = (GraphView) findViewById(R.id.graph);


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                graph.removeAllSeries();
                DD value;
                dataModels = new ArrayList<>();
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String[] data = d.child("TEMP").getValue().toString().split(" ");
//                    Last edit of the data activity
                    time = data[2].split("\\.");
                    value = new DD(Float.parseFloat(data[0]), time[0]);


                    dataModels.add(value);
                }


                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                        new DataPoint(0, dataModels.get(dataModels.size() - 5).getTEMP()),
                        new DataPoint(1, dataModels.get(dataModels.size() - 4).getTEMP()),
                        new DataPoint(2, dataModels.get(dataModels.size() - 3).getTEMP()),
                        new DataPoint(3, dataModels.get(dataModels.size() - 2).getTEMP()),
                        new DataPoint(4, dataModels.get(dataModels.size() - 1).getTEMP())
                });


                graph.addSeries(series);


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


    }
}
