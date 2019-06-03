package enachescurobert.com.ohleics.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//This will be used for retrofit
@IgnoreExtraProperties
public class PostSource {

    @SerializedName("_source")
    @Expose
    private Post post;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

}
