package app.bus2go.android.domain.use_case

import app.bus2go.android.domain.repository.AppStateRepository

class DeleteSelfSignedCertificates(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(): Boolean {
		return appStateRepository.removeSelfSignedCerts()
	}
}