package com.smartpods.android.pulseecho.Model
import org.json.JSONObject

open class LoginModel(var OrgCode: String,
                      var OrgName: String,
                      var Settings: ProfileObject,
                      var User: UserObject)