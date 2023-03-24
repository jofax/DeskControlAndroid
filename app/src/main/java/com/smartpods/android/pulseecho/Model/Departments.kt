package com.smartpods.android.pulseecho.Model

import org.json.JSONArray
import org.json.JSONObject

class Department(var ID: Int,
                 var Name: String) {

    override fun toString(): String {
        return this.Name
    }
}

open class Departments(var obj: JSONObject)  {
    var ListDepartments: MutableList<Department> = mutableListOf()
    var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(obj)

    init {
        if (obj.has("Departments") && !obj.isNull("Departments")) {
            val mDepartments = obj["Departments"] as JSONArray

            (0 until mDepartments.length()).forEach {
                val mObj = mDepartments.getJSONObject(it)
                ListDepartments.add(Department(mObj["ID"] as Int, mObj["Name"] as String))
            }

        }
    }
}