package in.tvac.akshayejh.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TempratureAdapter extends RecyclerView.Adapter<TempratureAdapter.DataViewHolder> {

    private FirebaseFirestore firebaseFirestore;


    private ArrayList<DataModel> data;
    private int rowLayout;
    private Context context;

    public static class DataViewHolder extends RecyclerView.ViewHolder{

        LinearLayout dataLayout;
        TextView waterTemprature;
        TextView dateAndTime;

        public DataViewHolder(View itemView) {
            super(itemView);
            waterTemprature=itemView.findViewById(R.id.water_temprature);
            dateAndTime = itemView.findViewById(R.id.date_and_time);
        }
    }

    public TempratureAdapter(ArrayList<DataModel> data, int rowLayout, Context context) {
        this.data = data;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new DataViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.waterTemprature.setText(data.get(position).getTEMP().toString());
        holder.dateAndTime.setText(data.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
