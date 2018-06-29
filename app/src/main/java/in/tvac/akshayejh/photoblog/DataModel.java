package in.tvac.akshayejh.photoblog;

import org.json.JSONObject;

import java.util.ArrayList;

public class DataModel {
   String TEMP ;
   String time ;

    public DataModel(String TEMP, String time) {
        this.TEMP = TEMP;
        this.time = time;
    }

    public DataModel() {
    }

    public String getTEMP() {
        return TEMP;
    }

    public void setTEMP(String TEMP) {
        this.TEMP = TEMP;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
