package in.tvac.akshayejh.photoblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class AdminFragment extends Fragment {


    Button setAlarmBTN , toTempretureTable ;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_admin,
                container, false);


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



        return view;
    }


}
