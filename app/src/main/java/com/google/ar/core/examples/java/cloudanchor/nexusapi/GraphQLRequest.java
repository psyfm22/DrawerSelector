package com.google.ar.core.examples.java.cloudanchor.nexusapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphQLRequest {
        @JsonProperty("query")
        private String query;

        @JsonProperty("variables")
        private Object variables;

        public GraphQLRequest(String query, Object variables) {
            this.query = query;
            this.variables = variables;
        }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Object getVariables() {
        return variables;
    }

    public void setVariables(Object variables) {
        this.variables = variables;
    }
}
