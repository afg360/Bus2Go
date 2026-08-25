package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.AppStateRepository
import java.security.cert.X509Certificate

class AcceptSelfSignedCertificate(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(cert: X509Certificate) {
		appStateRepository.setSelfSignedCert(cert)
	}
}
