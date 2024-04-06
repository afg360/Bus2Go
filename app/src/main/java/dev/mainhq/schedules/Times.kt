package dev.mainhq.schedules

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Times : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.times)
        val stopCode = intent.getIntExtra("stopCode", -1)
        assert (stopCode != -1)
        Toast.makeText(this, stopCode.toString(), Toast.LENGTH_SHORT).show()
    }
}