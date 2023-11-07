package com.example.communautoflexdetector.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiInterface {
    @GET( "GetAvailableVehicles" )
    fun getData(
        @Query("BranchID") branchId: Int,
        @Query("LanguageID") languageId: Int,
        @Query("cityID") cityId: Int
    ): Call<CommunautoData>
}

interface BookingInterface {
    @GET("CreateBooking")
    fun getData(
        @Header("Cookie") sessionId: String,
        @Query("CustomerID") customerId: Int,
        @Query("CarID") carId: Int
    ): Call<CommunautoData>
}