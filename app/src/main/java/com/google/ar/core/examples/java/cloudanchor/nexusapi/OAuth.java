package com.google.ar.core.examples.java.cloudanchor.nexusapi;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OAuth {
    @FormUrlEncoded
    @POST("connect/token")
    Call<TokenResponse> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("scope") String scope
    );

        @POST("graphql")
        @Headers("Content-Type: application/json")
        Call<JsonNode> queryGraphql(@Body GraphQLRequest request);
}
