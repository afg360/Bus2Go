package dev.mainhq.bus2go.fragments.alarms

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AlarmCreationChooseBusDialog() : DialogFragment(R.layout.fragment_create_alarms_choose_stop_dialog) {
    data class AlarmBusInfo(val busInfo : ExoBusData, val time : Time) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(ExoBusData::class.java.classLoader)!!,
            parcel.readParcelable(Time::class.java.classLoader)!!
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(busInfo, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
            //dest.writeParcelable(time, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        }

        companion object CREATOR : Parcelable.Creator<AlarmBusInfo> {
            override fun createFromParcel(parcel: Parcel): AlarmBusInfo {
                return AlarmBusInfo(parcel)
            }

            override fun newArray(size: Int): Array<AlarmBusInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var alarmDialogBusStop : String
    private lateinit var alarmDialogBusNum : String
    private lateinit var alarmDialogBusHeadsign : String
    private lateinit var favouritesViewModel: FavouritesViewModel

    private var time : Time? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favouritesViewModel = ViewModelProvider(requireActivity())[FavouritesViewModel::class.java]
        setStyle(STYLE_NORMAL, R.style.MaterialTheme_Bus2Go)//R.style.MaterialTheme_Bus2Go_Dark)
        (context as Activity).also {
            activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    time = if (VERSION.SDK_INT < 33) {
                        result.data?.getParcelableExtra("TIME")
                    } else{
                        result.data?.getParcelableExtra("TIME", Time::class.java)
                    }
                    time?.also {time ->
                        setFragmentResult("requestKey", bundleOf("ALARM_BUS_INFO" to AlarmBusInfo(
                            ExoBusData(alarmDialogBusStop, alarmDialogBusNum, "", "", ""),
                            time)
                        ))
                        dismiss()
                    }
                }
                //TODO do something with the choice and open up the next dialog, obv save the data
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** List all the favourite stops available for the user to create an alarm */
        lifecycleScope.launch {
            context?.also {
                val bottomNavBar = AlarmCreationDialogBottomNavBar()
                withContext(Dispatchers.Main) {
                    bottomNavBar.setBottomNavBarListener(object :
                        AlarmCreationDialogBottomNavBar.BottomNavBarListener {
                        override fun onCancel() {
                            dismiss()
                        }
                        override fun onAccept() {
                            if (view.findViewById<MaterialTextView>(R.id.acceptAlarmCreation).tag == AVAILABLE) {
                                view.findViewById<RecyclerView>(R.id.alarmDialogRecyclerView).forEach {
                                    if (it.findViewById<RadioButton>(R.id.addAlarmBusChoiceRadioButton).isChecked) {
                                        val intent = Intent(context, TimesActivity::class.java)
                                        alarmDialogBusStop = it.findViewById<MaterialTextView>(R.id.alarmDialogBusStop).text.toString()
                                        alarmDialogBusNum = it.findViewById<MaterialTextView>(R.id.alarmDialogBusNum).text.toString()
                                        intent.putExtra("stopName", alarmDialogBusStop)
                                        intent.putExtra(BusExtrasInfo.ROUTE_ID.name, alarmDialogBusNum)
                                        intent.putExtra("ALARMS", true)
                                        intent.putExtra(BusExtrasInfo.HEADSIGN.name, alarmDialogBusNum)
                                        activityResultLauncher.launch(intent)

                                        //val resultIntent = Intent()
                                        //resultIntent.putExtra("ALARM_BUS_INFO", AlarmBusInfo(
                                        //        BusInfo(alarmDialogBusStop, alarmDialogBusNum),
                                        //    time)
                                        //)
                                        //(context as Activity).setResult(Activity.RESULT_OK, resultIntent)
                                        //dismiss()
                                    }
                                }
                            }
                        }

                    })
                    childFragmentManager.beginTransaction()
                        .add(R.id.createAlarmsDialogChooseBusBottomNav, bottomNavBar)
                        .commit()
                     val list = favouritesViewModel.getAllBusInfo()
                     recyclerView = view.findViewById(R.id.alarmDialogRecyclerView)
                     val layoutManager = LinearLayoutManager(it)
                     layoutManager.orientation = LinearLayoutManager.VERTICAL
                     //recyclerView.adapter = AlarmDialogListElemsAdapter(list, recyclerView, bottomNavBar)
                     recyclerView.layoutManager = layoutManager
                }
            }
        }
    }

}