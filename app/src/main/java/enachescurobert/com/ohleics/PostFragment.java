package enachescurobert.com.ohleics;

import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import enachescurobert.com.ohleics.models.Post;
import enachescurobert.com.ohleics.util.UniversalImageLoader;

public class PostFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener{

    private static final String TAG = "PostFragment";

    //widgets
    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mStateProvince, mCity, mContactEmail;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes; //what we actually upload
    private double mProgress = 0; // upload progress rate

    @Override
    public void getImagePath(Uri imagePath) {
        Log.d(TAG, "getImagePath: setting the image to imageview");
        UniversalImageLoader.setImage(imagePath.toString(), mPostImage);
        //assign to global variable
        mSelectedBitmap = null;
        mSelectedUri = imagePath;

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        Log.d(TAG, "getImageBitmap: setting the image to imageview");
        mPostImage.setImageBitmap(bitmap);
        // assign to a global vairable
        mSelectedUri = null;
        mSelectedBitmap = bitmap;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        mPostImage = view.findViewById(R.id.post_image);
        mTitle = view.findViewById(R.id.input_title);
        mDescription = view.findViewById(R.id.input_description);
        mPrice = view.findViewById(R.id.input_price);
        mCountry = view.findViewById(R.id.input_country);
        mStateProvince = view.findViewById(R.id.input_state_province);
        mCity = view.findViewById(R.id.input_city);
        mContactEmail = view.findViewById(R.id.input_email);
        mPost = view.findViewById(R.id.btn_post);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();

        return view;
    }


    //This will be used for the dialog class
    private void init(){

        //click on image
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                //Create the dialog
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this, 1);

            }
        });

        //click on POST button
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to post...");
                if(!isEmpty(mTitle.getText().toString())
                        &&!isEmpty(mDescription.getText().toString())
                        &&!isEmpty(mPrice.getText().toString())
                        &&!isEmpty(mCountry.getText().toString())
                        &&!isEmpty(mStateProvince.getText().toString())
                        &&!isEmpty(mCity.getText().toString())
                        &&!isEmpty(mContactEmail.getText().toString()))
                {
                    //we have a bitmap and no Uri
                    if(mSelectedBitmap != null && mSelectedUri == null){
                        uploadNewPhoto(mSelectedBitmap);
                    }
                    //we have no bitmap and a Uri
                    else if(mSelectedBitmap == null && mSelectedUri != null){
                        uploadNewPhoto(mSelectedUri);
                    }
                }else{
                    Toast.makeText(getActivity(), "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadNewPhoto(Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto(Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);

    }

    //Compression of image
    //with a background task on the background thread
    //because when we convert a uri to a bitmap object, that can slow the UI thread
    //and when we compress the byte array, it can slow the UI thread
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]>{

        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if(bitmap != null){
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {

            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){
                // => we have an uri
                //and we need to get a bitmap from that Uri
                try{
                    mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uris[0]);
                }catch (IOException e){
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }

            byte[] bytes = null;

            //mBitmap.getByteCount()/ the number of bytes in a megabyte (1 million)
            Log.d(TAG, "doInBackground: megabytes before compression: " + mBitmap.getByteCount()/ 1000000);
            bytes = getBytesFromBitmap(mBitmap, 100);
            Log.d(TAG, "doInBackground: megabytes after the compression: " + bytes.length/ 1000000);
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            hideProgressBar();
            //execute the upload task
            executeUploadTask();

        }
    }

        private void executeUploadTask(){
            Toast.makeText(getActivity(), "uploading image", Toast.LENGTH_SHORT).show();

            //every post needs a post id reference
            final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

            //storage reference
            //.child -> the path in the storage directory in Firebase where the post will be saved
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("posts/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                            "/" + postId + "/post_image");

            final UploadTask uploadTask = storageReference.putBytes(mUploadBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Post Success", Toast.LENGTH_SHORT).show();

                    //now we need to get an Uri from the upload and store it to the db
                    //this will be the pointer that is pointing to
                    //where that image is going to be saved in storage

                    //insert the download url into the firebase database
                    //Uri firebaseUri = taskSnapshot.getDownloadUrl();

                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    Log.d(TAG, "onSuccess: firebase download url: " + firebaseUri.toString());

                    //Now we need to create the database to store the Url
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                    //now we want to create our new post object
                    //in order to store the pointer in the database
                    Post post = new Post();
                    post.setImage(firebaseUri.toString());
                    post.setCity(mCity.getText().toString());
                    post.setContact_email(mContactEmail.getText().toString());
                    post.setCountry(mContactEmail.getText().toString());
                    post.setDescription(mDescription.getText().toString());
                    post.setPost_id(postId);
                    post.setPrice(mPrice.getText().toString());
                    post.setState_province(mStateProvince.getText().toString());
                    post.setTitle(mTitle.getText().toString());
                    post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


                    //insert in the node posts, add another child for the post id
                    //and insert it to the database
                    reference.child(getString(R.string.node_posts))
                            .child(postId)
                            .setValue(post);

                    resetFields();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "could not upload photo", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //we need a number to represent how far we are in the upload process
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    //so it won't print out to often we will:
                    if( currentProgress > (mProgress + 15)){
                        mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: upload is " + mProgress +"% done");
                        Toast.makeText(getActivity(), mProgress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    //now we need to convert the bitmap to a byte array
    //then compress it
    //then we can upload it
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality,stream);
        return stream.toByteArray();
    }

    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage);
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mCountry.setText("");
        mStateProvince.setText("");
        mCity.setText("");
        mContactEmail.setText("");
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


}
