package in.tvac.akshayejh.photoblog;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmActivity extends AppCompatActivity {


    TextView tempratureAlarmTV;
    Button setAlarmBtn;

    EditText tempMin , tempMax ;

    SharedPreferences ShredRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        ShredRef = this.getSharedPreferences("CurrentUser", this.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(this.getResources().getString(R.string.alarm_panel_toolbar));

        tempratureAlarmTV = findViewById(R.id.temprature_alarmTV);
        tempratureAlarmTV.setText("Your current alarm limit is " + 27);


        tempMin = findViewById(R.id.temprature_min_alarmET);
        tempMax = findViewById(R.id.temprature_max_alarmET);

        setAlarmBtn = findViewById(R.id.set_alarm_btn);



        String tempContent="Min Temp : "+ShredRef.getString("Min","24") +",Max Temp:"+ShredRef.getString("Max","27") ;

        tempratureAlarmTV.setText(tempContent);

        tempMin.setText(ShredRef.getString("Min","24"));
        tempMax.setText(ShredRef.getString("Max","27"));




        setAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tempMin.getText().toString().isEmpty()){
                    tempMin.setError("Enter Minimum value..");

                }
                else if(tempMax.getText().toString().isEmpty()){
                    tempMax.setError("Enter Maximum value..");
                }
                else {

                    SharedPreferences.Editor editor=ShredRef.edit();
                    editor.putString("Min",tempMin.getText().toString());
                    editor.putString("Max",tempMax.getText().toString());
                    editor.commit();

                    String tempContent="Min Temp : "+ShredRef.getString("Min","24") +",Max Temp:"+ShredRef.getString("Max","27") ;

                    tempratureAlarmTV.setText(tempContent);


                    Toast.makeText(AlarmActivity.this, "Alarm Set Succesfull..", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
