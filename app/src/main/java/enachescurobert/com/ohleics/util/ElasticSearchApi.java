package enachescurobert.com.ohleics.util;

import java.util.Map;

import enachescurobert.com.ohleics.models.HitsObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface ElasticSearchApi {

    @GET("_search/")
    Call<HitsObject> search(
            @HeaderMap Map<String, String> headers,
            @Query("default_operator") String operator, //1st query -> this will automatically prepend a question mark ('?') to the url
            @Query("q") String query //2nd query -> every query after the first will prepend an '&' symbol
            );
}
