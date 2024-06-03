package dev.mainhq.bus2go.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.mainhq.bus2go.MainActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings
import dev.mainhq.bus2go.adapters.AlarmsListElemAdapter
import dev.mainhq.bus2go.preferences.Alarm
import dev.mainhq.bus2go.viewmodel.AlarmCreationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


/* For the moment, the user can only add an alarm to a favourite bus */
class Alarms(val alarmViewModel : AlarmCreationViewModel)
    : Fragment(R.layout.fragment_alarms)  {

    private lateinit var list : List<Alarm>
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuBar = view.findViewById<MaterialToolbar>(R.id.alarmsMaterialToolBar)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.settingsIcon, R.id.addAlarmButton -> {
                    val intent = Intent(this.context, Settings::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        context?.also {
            recyclerView = view.findViewById(R.id.alarmsList)
            val linearLayoutManager = LinearLayoutManager(it)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            lifecycleScope.launch {
                list = alarmViewModel.readAlarms()
                withContext(Dispatchers.Main){
                    recyclerView.adapter = AlarmsListElemAdapter(list)
                }
            }
        }

        context?.apply {
            //This recycler View is NOT in the xml. it is created for the dialog box specifically
            val recyclerView = RecyclerView(this)
            lifecycleScope.launch {
                val list =  favouritesDataStore.data.first().list.toList()
                withContext(Dispatchers.Main){
                    if (list.isNotEmpty()) {
                        val layoutManager = LinearLayoutManager(view.context)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {_ ->
                            //Create a popup containing the recyclerview of each buses in favourites
                            context?.also {
                                val alarmCreationDialog = AlarmCreationDialog(alarmViewModel)
                                //TODO DEPRECATED FOR DATA EXCHANGE alarmCreationDialog.setTargetFragment(this@Alarms, 0)
                                val transaction = parentFragmentManager.beginTransaction()
                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                transaction.add(alarmCreationDialog, null)
                                    .addToBackStack("ALARM_CREATION_DIALOG").commit()
                            }
                        }
                    }
                    else view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
                        .setOnClickListener{
                            Toast.makeText(view.context, "No favourites made yet", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        setFragmentResultListener("NEW_ALARM") { requestKey, bundle ->
            val newAlarm = bundle.getBoolean("ON_ACCEPT")
            if (newAlarm){
                lifecycleScope.launch {
                    val list = alarmViewModel.readAlarms()
                    withContext(Dispatchers.Main){
                        recyclerView.adapter = AlarmsListElemAdapter(list)
                    }
                }
            }
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        // Play alarm sound or trigger any action you want here.
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()
    }
}