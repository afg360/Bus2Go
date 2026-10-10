package app.bus2go.android.presentation.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Base activity class defining the correct theme to apply */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }

}