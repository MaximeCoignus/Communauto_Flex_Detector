package com.example.communautoflexdetector

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET( "GetAvailableVehicles" )
    fun getData(
        @Query("BranchID") branchId: Int,
        @Query("LanguageID") languageId: Int,
        @Query("cityID") cityId: Int
    ): Call<CommunautoData>
}