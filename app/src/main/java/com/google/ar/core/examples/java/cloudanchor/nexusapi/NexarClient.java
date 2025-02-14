package com.google.ar.core.examples.java.cloudanchor.nexusapi;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.ar.core.examples.java.cloudanchor.GraphQLRequest.Category;
import com.google.ar.core.examples.java.cloudanchor.GraphQLRequest.Result;
import com.google.ar.core.examples.java.cloudanchor.GraphQLRequest.SearchResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

//https://github.com/NexarDeveloper/nexar-first-supply-query/tree/main
//https://support.nexar.com/support/solutions/articles/101000467983-c-example-to-generate-an-access-token
public class NexarClient {
    private String accessToken;
    private TokenResponse tokenResponse;
    private static final String CLIENT_ID = "10d4ed50-9d20-47fe-a893-0dc1a7d32e4a";
    private static final String CLIENT_SECRET = "U7mwadfhkkdjDpBl7j6dkakPbK24XSrhQAWM";
    private static final String TOKEN_ENDPOINT = "https://identity.nexar.com/";
    private static final String NEXAR_ENDPOINT = "https://api.nexar.com/";

    private final Retrofit retrofit;

    public NexarClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(TOKEN_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
    }

    public void getAccessToken(AccessCallback accessCallback) throws Exception {
        OAuth oAuthService = retrofit.create(OAuth.class);
        Call<TokenResponse> call = oAuthService.getAccessToken("client_credentials","supply.domain");

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenResponse = response.body();
                    accessToken = tokenResponse.getAccessToken();
                    accessCallback.AccessFound("Successfully Retrieved Code");
                } else {
                    accessCallback.AccessFailed("Failed to get Token: "+ response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                accessCallback.AccessFailed("Failed to get Token");
            }
        });
    }

    public void Query(String component, QueryCallback queryCallback){

        GraphQLRequest graphQLRequest = getGraphQLRequest(component);
                Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NEXAR_ENDPOINT)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Authorization", "Bearer " + accessToken);
                            Request request1 = requestBuilder.build();
                            return chain.proceed(request1);
                        })
                        .build())
                .build();

        OAuth auth = retrofit.create(OAuth.class);
        Call<JsonNode> queryCall = auth.queryGraphql(graphQLRequest);
        queryCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //load the response of the query being loaded into a string
                    String responseString = response.body().toString();
                    Log.d("COMP3018",responseString);

                    //Initialise the gson and the assign the string response the custom search
                    //response class
                    Gson gson = new Gson();
                    SearchResponse searchResponse = gson.fromJson(responseString, SearchResponse.class);

                    //Initialise an array of categoryNames and categoryCounters. The category names
                    //is simply a list of strings with the names of the category of each element.
                    // The category counters holds the name, the id and the counter of how many of this
                    // type of components have appeared.
                    List<String> categoryNames = new ArrayList<>();
                    List<CategoryCounter> categoryCounters =  new ArrayList<>();

                    //This is just an array of the results from the response makes it more simple
                    List<Result> results = searchResponse.getData().getSupSearch().getResults();

                    //If empty we return from the query as we no no responses came from the search
                    if(results.isEmpty()){
                        queryCallback.QueryFailed("Failure, No Components Found");
                    }

                    Log.d("COMP3018", ""+ results.size());

                    //Scroll through each of the results
                    for(int i=0; i<results.size(); i++){
                        //Get the category of the specific result
                        Category category = results.get(i).getPart().getCategory();

                        //If the loaded category is null we skip it
                        if(category == null || category.getName().isEmpty()){
                            break;
                        }

                        //If this is the first inputted category then we add it to both arrays
                        if(categoryNames.isEmpty()) {
                            categoryNames.add(category.getName());
                            CategoryCounter categoryCounter = new CategoryCounter(category.getId(), category.getName());
                            categoryCounter.addCounter();
                            categoryCounters.add(categoryCounter);
                        }else{

                            if(categoryNames.contains(category.getName())){
                                for(int j=0 ; j<categoryCounters.size();j++){
                                    if(categoryCounters.get(j).getName().equals(category.getName())){
                                        categoryCounters.get(j).addCounter();
                                    }
                                }
                            }else {
                                categoryNames.add(category.getName());
                                CategoryCounter categoryCounter = new CategoryCounter(category.getId(),category.getName());
                                categoryCounter.addCounter();
                                categoryCounters.add(categoryCounter);
                            }
                        }
                    }

                    int index = 0;
                    int maxCounter = categoryCounters.get(0).getCounter();

                    for(int i=0;i<categoryCounters.size();i++){
                        if(categoryCounters.get(i).getCounter() > maxCounter){
                            index = i;
                        }
                        Log.d("COMP3018", "ID: "+categoryCounters.get(i).getId()+" Name: "+categoryCounters.get(i).getName()+" Counter: "+categoryCounters.get(i).getCounter());
                    }
                    queryCallback.QueryFound(categoryCounters.get(index).getName());

                } else {
                    Log.e("GraphQL", "Failed to get response: " + response.code());
                    queryCallback.QueryFailed("Failure in Finding Items");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonNode> call, @NonNull Throwable t) {
                queryCallback.QueryFailed("Failure in Finding Items");
                t.printStackTrace();
            }
        });
    }


    @NonNull
    private GraphQLRequest getGraphQLRequest(String component) {
        String searchQuery =
                """
                        query partSearch($q: String!, $limit: Int!) {
                           supSearch (
                           q: $q,\s
                           limit: $limit
                           ){\s
                           hits\s
                               results {\s
                                   part {
                                       id
                                       name
                                       mpn
                                       medianPrice1000 {
                                           quantity
                                           currency
                                       }
                                       category {
                                           id
                                           name
                                       }\
                                       manufacturer {
                                           name
                                           homepageUrl
                                       }
                                   }
                               }
                           }
                        }
                        """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("q", component);
        variables.put("limit", 5);
        return new GraphQLRequest(searchQuery, variables);
    }

    /**
     * CreateOkHttpClient,
     *
     *
     * @return OkHttpClient
     */
    private OkHttpClient createOkHttpClient() {
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder().header("Authorization", "Basic " + base64Credentials);
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }).build();
    }

    public interface AccessCallback {
        void AccessFound(String response);
        void AccessFailed(String errorMessage);

    }

    public interface QueryCallback {
        void QueryFound(String categoryName);
        void QueryFailed(String errorMessage);
    }
}