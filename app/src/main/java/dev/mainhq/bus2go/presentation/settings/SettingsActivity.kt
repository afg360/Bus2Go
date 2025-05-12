package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private val viewModel: SettingsSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val menuBar = findViewById<MaterialToolbar>(R.id.settingsToolBar)
        menuInflater.inflate(R.menu.app_bar_settings, menuBar.menu)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.settingsBackButton -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.fragmentUsed.collect{
                    when(it){
                        FragmentUsed.MAIN -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.preferencesFragmentContainer, SettingsMainFragment())
                                .commit()
                        }
                        FragmentUsed.UPDATES -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.preferencesFragmentContainer, SettingsUpdatesFragment())
                                .commit()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                val loadingBar = findViewById<CircularProgressIndicator>(R.id.preferences_loading_bar)
                viewModel.isLoading.collect{
                    if (it) loadingBar.visibility = View.VISIBLE
                    else loadingBar.visibility = View.GONE
                }
            }
        }

    }

    fun changeTheme(){
        finish()
        startActivity(intent)
    }

}
