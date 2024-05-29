package dev.mainhq.bus2go.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R


const val UNAVAILABLE = "UNAVAILABLE"
const val AVAILABLE = "AVAILABLE"

class AlarmCreationDialogBottomNavBar : Fragment(R.layout.fragment_create_alarms_dialog_bottom_nav) {

    interface BottomNavBarListener {
        fun onCancel()
        fun onAccept()
    }

    abstract class BottomNavBarAcceptStateChangeListener {
        open fun onAcceptStateChange(view : View){
            view.findViewById<MaterialTextView>(R.id.acceptAlarmCreation).apply {
               if (tag == UNAVAILABLE) setTextColor(resources.getColor(R.color.grey, null))
                else if (tag == AVAILABLE) setTextColor(null)
            }
        }
    }

    private lateinit var bottomNavBarListener: BottomNavBarListener
    private lateinit var acceptAlarmCreation : MaterialTextView

    fun setBottomNavBarListener(listener: BottomNavBarListener) {
        bottomNavBarListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialTextView>(R.id.cancelAlarmCreation).setOnClickListener {
            bottomNavBarListener.onCancel()
            acceptAlarmCreation
        }
        acceptAlarmCreation = view.findViewById(R.id.acceptAlarmCreation)
        acceptAlarmCreation.tag = UNAVAILABLE
        acceptAlarmCreation.setTextColor(resources.getColor(R.color.grey, null))
        acceptAlarmCreation.setOnClickListener {
            if (acceptAlarmCreation.tag == AVAILABLE) bottomNavBarListener.onAccept()
        }
    }

    fun activateAcceptAlarmButton(){
        acceptAlarmCreation.tag = AVAILABLE
        acceptAlarmCreation.setTextColor(resources.getColor(R.color.basic_blue, null))
    }
    fun deActivateAcceptAlarmButton(){
        acceptAlarmCreation.tag = UNAVAILABLE
        acceptAlarmCreation.setTextColor(resources.getColor(R.color.grey, null))
    }
}