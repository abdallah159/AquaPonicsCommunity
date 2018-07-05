package in.tvac.akshayejh.photoblog;

public class DD {


    Float TEMP ;
    String Time ;

    public DD() {
    }




    public DD(Float TEMP, String time) {
        this.TEMP = TEMP;
        Time = time;
    }


    public Float getTEMP() {
        return TEMP;
    }

    public void setTEMP(Float TEMP) {
        this.TEMP = TEMP;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
