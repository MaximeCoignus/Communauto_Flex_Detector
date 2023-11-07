package com.example.communautoflexdetector.ui.theme

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.communautoflexdetector.MainActivity
import com.example.communautoflexdetector.R
import com.example.communautoflexdetector.data.ProvincesAndCitiesMap
import com.example.communautoflexdetector.data.cityIds
import com.example.communautoflexdetector.data.provinceIds

class CustomSpinners(activity: MainActivity): AdapterView.OnItemSelectedListener {
    private var cityID : Int = 105
    private var provinceID : Int = 2
    private var citiesByProvince : String = "Ontario"
    private val activityName = activity

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val name = activityName.resources.getStringArray(R.array.provinces_array)[p2]
        citiesByProvince = name
        provinceID = provinceIds[name]!!

        val cities = activityName.resources.getStringArray(ProvincesAndCitiesMap[citiesByProvince]!!)
        val citySpinner : Spinner = activityName.findViewById(R.id.city_spinner)
        val cityAdapter = ArrayAdapter(activityName, android.R.layout.simple_spinner_item, cities)

        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val name = cities[p2]
                cityID = cityIds[name]!!
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    fun getCityId(): Int {
        return cityID
    }

    fun getProvinceId(): Int {
        return provinceID
    }
}