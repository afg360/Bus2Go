package app.bus2go.android.presentation.settings

import android.os.Bundle
import androidx.activity.viewModels
import app.bus2go.android.presentation.base.BaseActivity
import app.bus2go.android.R
import app.bus2go.android.databinding.SettingsActivityBinding
import app.bus2go.android.utils.launchViewModelCollectLatest
import app.bus2go.android.utils.makeGone
import app.bus2go.android.utils.makeVisible
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
