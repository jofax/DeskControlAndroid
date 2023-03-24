package com.smartpods.android.pulseecho.Fragments.Home

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.GENDER
import com.smartpods.android.pulseecho.Constants.LIFESTYLE
import com.smartpods.android.pulseecho.CustomUI.BMIDialogFragment
import com.smartpods.android.pulseecho.Model.Department
import com.smartpods.android.pulseecho.Model.UserObject
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.ViewModel.HeightProfileViewModel
import com.smartpods.android.pulseecho.ViewModel.UserProfileViewModel
import info.androidhive.fontawesome.FontDrawable
import kotlinx.android.synthetic.main.user_profile_fragment.*
import java.io.IOException
import android.text.Editable
import android.text.TextWatcher


class UserProfile : BaseFragment(), AdapterView.OnItemSelectedListener  {

    companion object {
        fun newInstance() = UserProfile()
    }

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var profileViewModel: HeightProfileViewModel

    var bmiValue: Double =  0.0
    var bmrValue: Double = 0.0
    var calorieValue: Double = 0.0
    var userStandingPosition: Int = 0
    var height: Int = 0
    var weight: Double = 0.0
    var selectedUnitHeight: String = "cm"
    var selectedUnitWeight: String = "kg"
    var gender: Int = 0
    var lifestyle: Int = 0
    var mDEpartmentID: Int = 0
    var mSelectedDepartment: Int = 0
    var mSelectedLifestyle: Int = 0
    var mselectedGender: Int = 0

    var mUpdatedheight: Int = 0
    var mUpdatedweight: Double = 0.0
    lateinit var mUser: UserObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        profileViewModel = ViewModelProvider(this).get(HeightProfileViewModel::class.java)
        // TODO: Use the ViewModel
        initializeUI()
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromService()
        fragmentNavTitle("Home", true, false)
    }

    fun initializeUI() {
        val syncIcon = FontDrawable(appContext, R.string.fa_sync_alt_solid, true, false)
        syncIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_gray))
        btnConvertHeight.setImageDrawable(syncIcon)
        btnConvertWeight.setImageDrawable(syncIcon)

        btnConvertHeight.setOnClickListener{

        }

        btnConvertWeight.setOnClickListener{

        }

        fun okAction() = DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        }


        btnEstCal.setOnClickListener{
            pulseMainActivity.showALertDialog(getString(R.string.calorie_title),
                getString(R.string.calorie_content),
                false,
                getString(R.string.btn_ok), okAction())
        }

        btnBMRInfo.setOnClickListener{
            pulseMainActivity.showALertDialog(getString(R.string.bmr_title),
                getString(R.string.bmr_content),
                false,
                getString(R.string.btn_ok), okAction())
        }

        btnBMIInfo.setOnClickListener{
            val dialog = BMIDialogFragment()
            val fragmentManager = pulseMainActivity.supportFragmentManager
            dialog.show(fragmentManager, "BMIDialogFragment")

        }

        btnConvertHeight.setOnClickListener{
            val alert = AlertView("", getString(R.string.action_sheet_title), AlertStyle.BOTTOM_SHEET)
            alert.addAction(AlertAction("Centimeters", AlertActionStyle.DEFAULT) {
                selectedUnitHeight = "cm"
                val mHeight = "${height.toString()} $selectedUnitHeight"
                mUpdatedheight = height
                txtHeight.setText(mHeight)
            })
            alert.addAction(AlertAction("Feet and Inches", AlertActionStyle.DEFAULT) {
                selectedUnitHeight = "ft,in"
                val heightValues: HashMap<String, Any> = Utilities.centimeterToFeet(height.toString())
                val mHeight = heightValues["data"] as String
                val heightToCm :HashMap<String, Any> = Utilities.feetToCentimeter(mHeight)
                mUpdatedheight = heightToCm["raw"] as Int
                txtHeight.setText(mHeight)
            })

            alert.show(pulseMainActivity)
        }

        btnConvertWeight.setOnClickListener {
            val alert = AlertView("", getString(R.string.action_sheet_title), AlertStyle.BOTTOM_SHEET)
            alert.addAction(AlertAction("Kilogram", AlertActionStyle.DEFAULT) {
                selectedUnitWeight = "kg"
                val mWeight = "${weight.toString()} $selectedUnitWeight"
                mUpdatedweight = weight
                txtWeight.setText(mWeight)
            })
            alert.addAction(AlertAction("Pounds", AlertActionStyle.DEFAULT) {
                selectedUnitWeight = "lb"
                val mWeight = Utilities.kilogramsToPounds(weight)
                mUpdatedweight = Utilities.poundsToKilograms(mWeight.toDouble()).toDouble()
                txtWeight.setText("$mWeight $selectedUnitWeight")
            })

            alert.show(pulseMainActivity)
        }

        btnSaveProfile.setOnClickListener {
            requestUpdateUserInformation()
        }
    }

    fun fetchDataFromService() {

        if (view != null) else return

        val email = Utilities.getLoggedEmail()

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    //request user information from cloud
                    viewModel.requestUserDetails(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)
                        if (it.GenericResponse.Success) {
                            if (it != null) {
                                updateUI(it)
                            }
                        } else {
                            errorResponse(it.GenericResponse,email)
                        }
                    })

                } else {
                    //fetch locally saved profile settings
                    val mUser = viewModel.getUserLocally(email)
                    val mUserObj = UserObject(Utilities.toJSONParameters(mUser))
                    updateUI(mUserObj)
                }


            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }


        //get profile settings
        val profileSettings = profileViewModel.getProfileSettings(email)

        if (profileSettings != null) {
            userStandingPosition = profileSettings.StandingTime1 + profileSettings.StandingTime2
        }




    }

    fun requestUpdateUserInformation() {

        val email = Utilities.getLoggedEmail()

        mUser.Gender = mselectedGender
        mUser.DepartmentID = mSelectedDepartment
        mUser.LifeStyle = mSelectedLifestyle
        mUser.Height = mUpdatedheight
        mUser.Weight = mUpdatedweight

        view?.let { Utilities.hideSoftKeyBoard(appContext, it) }

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    viewModel.requestUpdateUser(email, mUser).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)
                        if (it.GenericResponse.Success) {
                            if (it != null) {
                                pulseMainActivity.showToastView("User profile has been updated.")
                            }
                        } else {
                            errorResponse(it.GenericResponse, email)
                        }
                    })


                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }





    }

    fun getDepartmentList() {
        val email = Utilities.getLoggedEmail()

        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = PulseApp.appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    viewModel.requestDepartmentList(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)
                        if (it.GenericResponse.Success) {
                            if (it != null) {

                                updateDepartmentList(it.ListDepartments)
                            }
                        } else {
                            errorResponse(it.GenericResponse, email)
                        }
                    })
                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    fun updateLifestyle(id: Int) {
        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.lifestyle_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                lifestyleSpinner.adapter = adapter
                lifestyleSpinner.onItemSelectedListener = this
            }
        }

        val lifestyleArr = resources.getStringArray(R.array.lifestyle_array)
//        (lifestyleArr.indices).forEach { }

        for (index in lifestyleArr.indices) {
            if (index == lifestyle) {
                txtLifestyle.setText(" ")
                txtLifestyle.setText(lifestyleArr.get(index).toString())
                break
            }
        }
    }

    fun updateGender(id: Int) {
        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                genderSpinner.adapter = adapter
                genderSpinner.onItemSelectedListener = this
            }
        }

        val genderArr = resources.getStringArray(R.array.gender_array)

        for (index in genderArr.indices) {
            if (index == lifestyle) {
                txtGender.setText(" ")
                txtGender.setText(genderArr.get(index).toString())
                break
            }
        }
    }

    fun updateDepartmentList(list: MutableList<Department>) {
        this.context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_item,
                list).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    departmentListSpinner.adapter = adapter
                    departmentListSpinner.onItemSelectedListener = this

                for (item in list) {
                    if (item.ID == mDEpartmentID) {
                        txtDepartment.setText(" ")
                        txtDepartment.setText(item.Name)
                        break
                    }
                }
            }
        }
    }

    fun updateUI(obj: UserObject) {
        mUser = obj

        txtFirstname.setText(obj.Firstname)

        txtLastname.setText(obj.Lastname)

        val mGender = GENDER(obj.Gender)?.rawValue
        val mLifestyle = LIFESTYLE(obj.LifeStyle)?.rawValue

        updateLifestyle(obj.LifeStyle)
        updateGender(obj.Gender)

        gender = obj.Gender
        lifestyle = obj.LifeStyle
        mDEpartmentID = obj.DepartmentID
        mSelectedDepartment = obj.DepartmentID
        mSelectedLifestyle = obj.LifeStyle
        mselectedGender = obj.Gender
        bmrValue = obj.BMR
        bmiValue = obj.BMI
        height = obj.Height
        weight = obj.Weight

        mUpdatedheight = height
        mUpdatedweight = weight

        txtGender.setText(mGender)
        txtDobs.setText(obj.YearOfBirth.toString())
        val mHeight = "${obj.Height.toString()} $selectedUnitHeight"
        val mWeight = "${obj.Weight.toString()} $selectedUnitWeight"

        txtHeight.setText(mHeight)
        txtWeight.setText(mWeight)

        lblBmiValue.text = "%.0f".format(obj.BMI)
        lblBMRValue.text = "%.0f".format(obj.BMR)

        getDepartmentList()
        calculateCalories()

        textfieldListeners()
    }

    fun calculateCalories() {
        var calories: Double = 0.0
        val standingInMinutes = userStandingPosition * Constants.hoursPerDayActivity

        if (bmrValue >= 0) {
            calories = (bmrValue / (60 * 24)) * standingInMinutes.toDouble()
        } else {
            calories = (0.095 * (weight + 3.1) * Constants.kiloJoulsToKiloCalories * standingInMinutes.toDouble())
        }

        calorieValue = calories
        lblEstCalValue.text = "%.0f".format(calories)

    }

    fun textfieldListeners() {
        //firstname
        txtFirstname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                mUser.Firstname = s.toString()
            }
        })

        //lastname
        txtLastname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                mUser.Lastname = s.toString()
            }
        })

        //lastname
        txtDobs.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                mUser.YearOfBirth = s.toString().toInt()
            }
        })
    }

    //Spinner Required Methods
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when(parent) {
            departmentListSpinner -> {
                txtDepartment.setText(" ")
                departmentListSpinner.getItemAtPosition(position).toString()
                val mDepartment = departmentListSpinner.getItemAtPosition(position) as Department
                mSelectedDepartment = mDepartment.ID

                //txtDepartment.text?.clear()
                //txtDepartment.setText(departmentListSpinner.getItemAtPosition(position).toString())

            }
            lifestyleSpinner -> {
                txtLifestyle.setText(" ")
                mSelectedLifestyle = position
            }
            genderSpinner -> {
                txtGender.setText(" ")
                mselectedGender = position
            }

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}