package dev.mainhq.bus2go.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import dev.mainhq.bus2go.R


private const val UNSELECTED = "UNSELECTED"
private const val SELECTED = "SELECTED"

class AlarmCreationDialog : DialogFragment(R.layout.fragment_create_alarms_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Schedules_Dark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialTextView>(R.id.textViewChooseStop).setOnClickListener{
            context?.also {
                val alarmCreationChooseBusDialog = AlarmCreationChooseBusDialog()
                //TODO DEPRECATED FOR DATA EXCHANGE alarmCreationDialog.setTargetFragment(this@Alarms, 0)
                val transaction = parentFragmentManager.beginTransaction()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction.add(alarmCreationChooseBusDialog, null).commit()
            }
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

