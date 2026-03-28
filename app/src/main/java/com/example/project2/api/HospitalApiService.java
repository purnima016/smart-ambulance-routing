package com.example.project2.api;

import com.example.project2.model.Hospital;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface HospitalApiService {
    @GET("hospitals/nearby")
    Call<List<Hospital>> getNearbyHospitals(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("radius") int radiusKm
    );

    // You can add more API endpoints here
    @GET("hospitals/search")
    Call<List<Hospital>> searchHospitals(
            @Query("query") String query,
            @Query("lat") double latitude,
            @Query("lng") double longitude
    );
}