package dev.mainhq.bus2go.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.Time


private const val UNSELECTED = "UNSELECTED"
private const val SELECTED = "SELECTED"

class AlarmCreationDialog : DialogFragment(R.layout.fragment_create_alarms_dialog) {

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private var alarmBusInfo : AlarmCreationChooseBusDialog.AlarmBusInfo? = AlarmCreationChooseBusDialog
        .AlarmBusInfo(
        BusInfo("BusName","TripHeadSign"),
            Time(0,0,0)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Schedules_Dark)


        (context as Activity).also {
            //FIXME DO IT FOR A FRAGMENT NOT AN ACTIVITY
            activityResultLauncher = registerForActivityResult(ActivityResultContracts
                .StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    alarmBusInfo = if (Build.VERSION.SDK_INT < 33) {
                        result.data?.getParcelableExtra("ALARM_BUS_INFO")!!
                    } else{
                        result.data?.getParcelableExtra("", AlarmCreationChooseBusDialog.AlarmBusInfo::class.java)!!
                    }
                }
                //TODO do something with the choice and open up the next dialog, obv save the data
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialTextView>(R.id.textViewChooseStop).setOnClickListener{_ ->
            val alarmCreationChooseBusDialog = AlarmCreationChooseBusDialog()
            //TODO DEPRECATED FOR DATA EXCHANGE alarmCreationDialog.setTargetFragment(this@Alarms, 0)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(alarmCreationChooseBusDialog, null).commit()
            setFragmentResultListener("requestKey") { requestKey, bundle ->
                alarmBusInfo = bundle.getParcelable<AlarmCreationChooseBusDialog.AlarmBusInfo?>("ALARM_BUS_INFO")
                    ?.also {
                    Log.d("ALARM BUS", it.toString())
                }
            }
            //FIXME supposed to pause here
            /** Retrieve the data, viewModel could be used here to restore
             *  state when going back to switch busStop */
            //view.findViewById<MaterialTextView>(R.id.textViewChooseStop)
            //    .text = alarmBusInfo.toString()
        }


        /*
        /** Create date picker  */
        val chooseDateTextView = view.findViewById<MaterialTextView>(R.id.chooseDateTextView)
        chooseDateTextView.setOnClickListener{
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Days when the alarm is active")
                .build()
            //datePicker.addOnPositiveButtonClickListener {
            //    datePicker
            //}
            datePicker.show(parentFragmentManager, null)
        }

         */

        /** Create time before picker */
        val chooseTimeTextView = view.findViewById<MaterialTextView>(R.id.chooseTimeTextView)
        chooseTimeTextView.setOnClickListener{
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setMinute(0)
                .setTitleText("Select Time Before The Bus")
                .setInputMode(INPUT_MODE_KEYBOARD)
                .build()

            timePicker.addOnCancelListener {
                it.cancel()
            }
            timePicker.addOnPositiveButtonClickListener {
                chooseTimeTextView.text = "${timePicker.hour} h, ${timePicker.minute} min before choosen bus time"
                //Log.d("TIME DATA", timePicker.hour.toString())
                //Log.d("TIME DATA", timePicker.minute.toString())
            }
            timePicker.show(parentFragmentManager, null)
        }

        /** Create day of the week picker */
        view.findViewById<LinearLayout>(R.id.chooseDatesRadios).children.forEach {
            //For each Radio button
            val radioButton = (it as RelativeLayout)[0] as RadioButton
            radioButton.tag = UNSELECTED
            radioButton.setOnClickListener  {_ ->
                if (radioButton.tag == UNSELECTED) radioButton.tag = SELECTED
                else {
                    radioButton.isChecked = false
                    radioButton.tag = UNSELECTED
                }
            }
        }

        val bottomNavBar = AlarmCreationDialogBottomNavBar()
        bottomNavBar.setBottomNavBarListener(object : AlarmCreationDialogBottomNavBar.BottomNavBarListener{
            override fun onCancel() {
                dismiss()
            }

            override fun onAccept() {
            }

        })
        childFragmentManager.beginTransaction()
            .add(R.id.createAlarmsDialogBottomNav, bottomNavBar)
            .commit()
    }
}

