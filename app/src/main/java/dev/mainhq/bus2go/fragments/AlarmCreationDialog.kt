package dev.mainhq.bus2go.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.utils.adapters.AlarmDialogListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("RestrictedApi")
class AlarmCreationDialog : DialogFragment(R.layout.fragment_alarms_dialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Schedules_Dark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            context?.also {
                val list = it.favouritesDataStore.data.first().list.toList()
                 withContext(Dispatchers.Main) {
                    val recyclerView: RecyclerView = view.findViewById(R.id.alarmDialogRecyclerView)
                    val layoutManager = LinearLayoutManager(it)
                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                    recyclerView.adapter = AlarmDialogListElemsAdapter(list, recyclerView)
                    recyclerView.layoutManager = layoutManager
                }
            }

        }

    }

}

