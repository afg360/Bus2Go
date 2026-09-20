package dev.mainhq.bus2go.presentation.config;

import android.content.Intent
import android.os.Bundle;
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.ConfigActivityBinding
import dev.mainhq.bus2go.databinding.MainActivityBinding
import dev.mainhq.bus2go.presentation.main.MainActivity
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeVisible

//todo set lang
//todo set color theme and or icon
//todo set notifs on or off (mostly for updates)
//todo set autoupdate

class ConfigActivity : BaseActivity() {

    private val viewModel: ConfigSharedViewModel by viewModels()

    private lateinit var binding: ConfigActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launchViewModelCollectLatest(viewModel.currentFragment) { fragmentToUse ->
            val fragment = when (fragmentToUse) {
				FragmentUsed.WELCOME -> ConfigWelcomeFragment()
				FragmentUsed.THEME -> ConfigThemeFragment()
				FragmentUsed.SERVER -> ConfigServerFragment()
				FragmentUsed.DATABASES -> ConfigDatabasesFragment()
				FragmentUsed.NOTIFICATIONS -> ConfigNotificationsFragment()
			}

			supportFragmentManager.beginTransaction()
				.replace(binding.configActivityFragmentContainer.id, fragment)
				.commit()
		}

        launchViewModelCollectLatest(viewModel.configButtonState) { configButtonState ->
            when(configButtonState) {
				is ConfigButtonState.Next -> {
                    binding.configButtonProgressIndicator.makeGone()
                    binding.configContinueButton.makeVisible()
                    binding.configContinueButton.text = configButtonState.text
					binding.configContinueButton.isEnabled = true
                    binding.configContinueButton.setOnClickListener {
                        configButtonState.onClick()
                    }
                }
				is ConfigButtonState.Skip -> {
                    binding.configButtonProgressIndicator.makeGone()
                    binding.configContinueButton.makeVisible()
					binding.configContinueButton.isEnabled = true
                    binding.configContinueButton.text = "Skip"
                    binding.configContinueButton.setOnClickListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Skip?")
                            .setMessage("Are you sure you want to skip? (You may configure this later).")
                            .setPositiveButton("Yes"){ dialogInterface, _ ->
                                configButtonState.onClick()
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                                dialogInterface.dismiss()
                            }
                            .setNegativeButton("Cancel"){ dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }
                            .show()
                    }
                }
				ConfigButtonState.Loading -> {
                    binding.configButtonProgressIndicator.makeVisible()
					binding.configContinueButton.isEnabled = false
                    binding.configContinueButton.text = ""
                }
            }
        }
    }
}
