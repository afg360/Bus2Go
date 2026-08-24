package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.SettingsActivityBinding
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

class SettingsActivity() : BaseActivity() {

    private val viewModel: SettingsSharedViewModel by viewModels()

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val menuBar = binding.settingsToolBar
        menuInflater.inflate(R.menu.app_bar_settings, menuBar.menu)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.settingsBackButton -> {
                    when(viewModel.fragmentUsed.value) {
						FragmentUsed.MAIN -> {
                            finish()
                        }
						FragmentUsed.UPDATES -> {
                            viewModel.setFragment(FragmentUsed.MAIN)
                        }
                        FragmentUsed.DATABASE_DOWNLOADS -> {
                            viewModel.setFragment(FragmentUsed.UPDATES)
                        }
					}
                    true
                }
                else -> false
            }
        }

        launchViewModelCollectLatest(viewModel.fragmentUsed) {
            supportFragmentManager
                .beginTransaction()
                .apply {
                    when(it){
                        FragmentUsed.MAIN -> {
                            replace(R.id.settings_fragment_container_view, SettingsMainFragment())
                        }
                        FragmentUsed.UPDATES -> {
                            replace(R.id.settings_fragment_container_view, SettingsUpdatesFragment())
                        }
                        FragmentUsed.DATABASE_DOWNLOADS -> {
                            replace(R.id.settings_fragment_container_view, SettingsDownloadDatabasesFragment())
                        }
                    }
                }
                .commit()
        }

        launchViewModelCollectLatest(viewModel.isLoading) {
            if (it) {
                binding.preferencesLoadingBar.makeVisible()
            }
            else {
                binding.preferencesLoadingBar.makeGone()
            }
        }
    }
}
