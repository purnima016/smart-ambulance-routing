package com.example.project2.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleDistanceMatrixService {

    @GET("maps/api/distancematrix/json")
    Call<DistanceMatrixResponse> getDistanceMatrix(
            @Query("origins") String origins,
            @Query("destinations") String destinations,
            @Query("key") String apiKey
    );

    class DistanceMatrixResponse {
        public String status;
        public String[] origin_addresses;
        public String[] destination_addresses;
        public Row[] rows;
    }

    class Row {
        public Element[] elements;
    }

    class Element {
        public String status;
        public Distance distance;
        public Duration duration;
    }

    class Distance {
        public String text;
        public int value;
    }

    class Duration {
        public String text;
        public int value;
    }
}