package enachescurobert.com.ohleics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import enachescurobert.com.ohleics.models.Post;
import enachescurobert.com.ohleics.util.UniversalImageLoader;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    //widgets
    private TextView mContactSeller, mTitle, mDescription, mPrice, mLocation, mSavePost;
    private ImageView mClose, mWatchList, mPostImage;

    //vars
    private String mPostId;
    private Post mPost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostId = (String) getArguments().get(getString(R.string.arg_post_id));
        Log.d(TAG, "onCreate: got the post id: " + mPostId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mContactSeller = (TextView) view.findViewById(R.id.post_contact);
        mTitle = (TextView) view.findViewById(R.id.post_title);
        mDescription =(TextView) view.findViewById(R.id.post_description);
        mPrice = (TextView) view.findViewById(R.id.post_price);
        mLocation = (TextView) view.findViewById(R.id.post_location);
        mClose = (ImageView) view.findViewById(R.id.post_close);
        mWatchList = (ImageView) view.findViewById(R.id.add_watch_list);
        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mSavePost = (TextView) view.findViewById(R.id.save_post);

        init();

        hideSoftKeyboard();

        return view;
    }


    //we need to retrieve the data using the mPostId
    //we will query the db and retrieve the rest of the info for the post

    private void init(){
        getPostInfo();


    }

    private void getPostInfo(){
        Log.d(TAG, "getPostInfo: getting the post information.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_posts))
                .orderByKey()
                .equalTo(mPostId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                if(singleSnapshot != null){
                    mPost = singleSnapshot.getValue(Post.class);
                    Log.d(TAG, "onDataChange: found the post: " + mPost.getTitle());

                    mTitle.setText(mPost.getTitle());
                    mDescription.setText(mPost.getDescription());

                    String price = "FREE";
                    if(mPost.getPrice() != null){
                        price = "$" + mPost.getPrice();
                    }
                    mPrice.setText(price);

                    String location = mPost.getCity() + ", " + mPost.getState_province() + ", " +
                            mPost.getCountry();
                    mLocation.setText(location);
                    mTitle.setText(mPost.getTitle());

                    UniversalImageLoader.setImage(mPost.getImage(), mPostImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //is responsible to closing the window when navigating to the fragment
    private void hideSoftKeyboard(){
        final Activity activity = getActivity();
        final InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
