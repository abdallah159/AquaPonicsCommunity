package in.tvac.akshayejh.photoblog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;

    String userName ;
    String userImage ;

    long millisecond ;
    String dateString ;
    String user_id ;


    TextView userNameDialog , dateDialog , descDialog , commentsDialog , likesDialog;
    CircleImageView userImageDialog ;

    ImageView blogPostImageDialog ;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    Dialog myDialog ;

    public NotificationRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();


        String image_url = blog_list.get(position).getImage_url();
        String thumbUri = blog_list.get(position).getImage_thumb();

         user_id = blog_list.get(position).getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                     userName = task.getResult().getString("name");
                     userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);


                } else {

                    //Firebase Exception

                }

            }
        });


        myDialog = new Dialog(context);
        myDialog.setContentView(R.layout.notification_content);





        try {
             millisecond = blog_list.get(position).getTimestamp().getTime();
             dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }



        userNameDialog = myDialog.findViewById(R.id.blog_user_name);
        dateDialog = myDialog.findViewById(R.id.date_and_time);
        likesDialog = myDialog.findViewById(R.id.blog_like_count);
        commentsDialog = myDialog.findViewById(R.id.blog_comment_count);
        descDialog = myDialog.findViewById(R.id.blog_desc);

        userImageDialog = myDialog.findViewById(R.id.blog_user_image);
        blogPostImageDialog = myDialog.findViewById(R.id.blog_image);





        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){

                            userName = task.getResult().getString("name");
                            userImage = task.getResult().getString("image");

                            holder.setUserData(userName, userImage);


                        } else {

                            //Firebase Exception

                        }

                    }
                });


                RequestOptions placeholderOption = new RequestOptions();
                placeholderOption.placeholder(R.drawable.profile_placeholder);

                Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(blog_list.get(position).getImage_url()).into(blogPostImageDialog);



                myDialog.show();

            }
        });



    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;


        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;


        private ConstraintLayout item ;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            item = mView.findViewById(R.id.notification_item_id);


        }



        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image){

            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);
            userNameDialog.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(userImageDialog);

        }

    }

}
