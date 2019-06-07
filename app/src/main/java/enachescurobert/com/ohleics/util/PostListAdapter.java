package enachescurobert.com.ohleics.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import enachescurobert.com.ohleics.R;
import enachescurobert.com.ohleics.SearchActivity;
import enachescurobert.com.ohleics.SearchFragment;
import enachescurobert.com.ohleics.WatchListFragment;
import enachescurobert.com.ohleics.models.Post;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder>{

    private static final String TAG = "PostListAdapter";
    private static final int NUM_GRID_COLUMNS = 3;

    private ArrayList<Post> mPosts;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder{

        SquareImageView mPostImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mPostImage = (SquareImageView) itemView.findViewById(R.id.post_image);

            int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int imageWidth = gridWidth/NUM_GRID_COLUMNS;
            mPostImage.setMaxHeight(imageWidth);
            mPostImage.setMaxWidth(imageWidth);
        }
    }

    public PostListAdapter(Context context, ArrayList<Post> posts) {
        mPosts = posts;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_view_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        UniversalImageLoader.setImage(mPosts.get(position).getImage(), holder.mPostImage);

        final int pos = position;
        holder.mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: selected a post");
                //TODO

                //view the post in more detail
                Fragment fragment = (Fragment)((SearchActivity)mContext).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewpager_container+ ":" +
                                ((SearchActivity)mContext).mViewPager.getCurrentItem());

                //fragment 0 is in the search fragment
                //fragment 1 is in the watch list fragment

                if(fragment != null){
                    //SearchFragment (#0)
                    if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")){
                        Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_view_post));

                        SearchFragment searchFragment = (SearchFragment)((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container+ ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());
                        searchFragment.viewPost(mPosts.get(pos).getPost_id());
                    }
                    //Watchlist Fragment( #1)
                    else if(fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")){
                        Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_watch_list));

                        WatchListFragment watchListFragment = (WatchListFragment) ((SearchActivity)mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container+ ":" +
                                        ((SearchActivity)mContext).mViewPager.getCurrentItem());
                        watchListFragment.viewPost(mPosts.get(pos).getPost_id());

                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }


}
