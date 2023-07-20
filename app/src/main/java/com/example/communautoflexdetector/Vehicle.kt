package com.example.communautoflexdetector

data class Vehicle(
    val BoardComputerType: Int,
    val BookingStatus: Int,
    val CarAccessories: List<Int>,
    val CarBrand: String,
    val CarColor: String,
    val CarId: Int,
    val CarModel: String,
    val CarNo: Int,
    val CarPlate: String,
    val CarSeatNb: Int,
    val CarVin: String,
    val CityID: Int,
    val EnergyLevel: Any,
    val IsElectric: Boolean,
    val LastUse: Int,
    val LastUseDate: String,
    val Latitude: Double,
    val Longitude: Double,
    val VehiclePromotions: Any,
    val isPromo: Boolean
)