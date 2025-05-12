package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat
import dev.mainhq.bus2go.R

class SettingsUpdatesFragment: PreferenceFragmentCompat() {

	private val sharedViewModel: SettingsSharedViewModel by activityViewModels()

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings_updates, rootKey)

		requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.MAIN)
				isEnabled = false
			}
		})
	}
}