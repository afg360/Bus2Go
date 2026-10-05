package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.AppStateRepository

class DeleteSelfSignedCertificates(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(): Boolean {
		return appStateRepository.removeSelfSignedCerts()
	}
}