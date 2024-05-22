package dev.mainhq.bus2go.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.MenuProvider
import com.google.android.material.appbar.MaterialToolbar
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings

/* For the moment, the user can only add an alarm to a favourite bus */
class Alarms : Fragment(R.layout.fragment_alarms) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuBar = view.findViewById<MaterialToolbar>(R.id.alarmsMaterialToolBar)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.addAlarmButton -> {
                    Toast.makeText(view.context, "Hello", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.settingsIcon -> {
                    val intent = Intent(this.context, Settings::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }



}