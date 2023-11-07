package com.example.communautoflexdetector.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AvailableCarsDataSource {
    private val baseUrl = "https://www.reservauto.net/WCF/LSI/LSIBookingServiceV3.svc/"

    private lateinit var listOfCars: List<Vehicle>

    private suspend fun fetchAvailableCars(provinceId: Int, cityId: Int): List<Vehicle> = withContext(Dispatchers.IO) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
            .create(ApiInterface::class.java)

        val result = retrofitBuilder.getData(provinceId, 2, cityId)

        val body = result.execute().body()!!
        return@withContext body.d.Vehicles
    }

    suspend fun createBooking(carId: Int): Response<CommunautoData> = withContext(Dispatchers.IO) {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
            .create(BookingInterface::class.java)

        retrofitBuilder.getData("mySession=7548ae2a-9629-4914-af1e-3a0fb986af42", 528322, carId).execute()
    }

    suspend fun getAvailableCars(provinceId: Int, cityId: Int) : List<Vehicle> {
        var counter = 0
        listOfCars = listOf()
        while (listOfCars.isEmpty() && counter < 300) {
            listOfCars = fetchAvailableCars(provinceId, cityId)
            counter++
        }
        return listOfCars
    }
}