package in.tvac.akshayejh.photoblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends AppCompatActivity {


    TextView tempratureAlarmTV;
    TextView tempratureAlarmET;
    Button setAlarmBtn;
    Button cancelAlarmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alarm Panel");

        tempratureAlarmTV = findViewById(R.id.temprature_alarmTV);
        tempratureAlarmTV.setText("Your current alarm limit is " + 27);

        tempratureAlarmET=findViewById(R.id.temprature_alarmET);


    }
}
