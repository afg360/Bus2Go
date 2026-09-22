package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import androidx.activity.viewModels
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.SettingsActivityBinding
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeVisible
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
						FragmentUsed.NOTIFICATIONS -> {
                            viewModel.setFragment(FragmentUsed.MAIN)
                        }
                        FragmentUsed.DATABASE_DOWNLOADS -> {
                            viewModel.setFragment(FragmentUsed.MAIN)
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
                        FragmentUsed.NOTIFICATIONS -> {
                            replace(R.id.settings_fragment_container_view, SettingsNotificationsFragment())
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
