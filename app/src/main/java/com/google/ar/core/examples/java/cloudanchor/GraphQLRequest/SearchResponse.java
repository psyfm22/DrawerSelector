package com.google.ar.core.examples.java.cloudanchor.GraphQLRequest;

//https://github.com/NexarDeveloper/nexar-first-supply-query/blob/main/Java-Spring-boot/src/main/java/com/coding/way/nexar/models/GraphQLResponse.java
public class SearchResponse{
    private Data data;
    private Extensions extensions;


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Extensions getExtensions() {
        return extensions;
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }
}

