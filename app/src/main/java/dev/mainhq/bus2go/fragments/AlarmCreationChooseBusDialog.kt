package dev.mainhq.bus2go.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.forEach
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.adapters.AlarmDialogListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class AlarmCreationChooseBusDialog() : DialogFragment(R.layout.fragment_create_alarms_choose_stop_dialog) {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Schedules_Dark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** List all the favourite stops available for the user to create an alarm */
        lifecycleScope.launch {
            context?.also {
                val job = async{it.favouritesDataStore.data.first().list.toList()}
                val bottomNavBar = AlarmCreationDialogBottomNavBar()
                withContext(Dispatchers.Main) {
                    bottomNavBar.setBottomNavBarListener(object : AlarmCreationDialogBottomNavBar.BottomNavBarListener{
                        override fun onCancel() {
                            dismiss()
                        }
                        override fun onAccept() {
                            if (view.findViewById<MaterialTextView>(R.id.acceptAlarmCreation).tag == AVAILABLE) {
                                view.findViewById<RecyclerView>(R.id.alarmDialogRecyclerView).forEach {
                                    if (it.findViewById<RadioButton>(R.id.addAlarmBusChoiceRadioButton).isChecked) {

                                        //TODO do something with the choice and open up the next dialog, obv save the data
                                    }
                                }
                            }
                        }

                    })
                    childFragmentManager.beginTransaction()
                        .add(R.id.createAlarmsDialogChooseBusBottomNav, bottomNavBar)
                        .commit()
                     val list = job.await()
                     recyclerView = view.findViewById(R.id.alarmDialogRecyclerView)
                     val layoutManager = LinearLayoutManager(it)
                     layoutManager.orientation = LinearLayoutManager.VERTICAL
                     recyclerView.adapter = AlarmDialogListElemsAdapter(list, recyclerView, bottomNavBar)
                     recyclerView.layoutManager = layoutManager
                }
            }
        }


    }

}