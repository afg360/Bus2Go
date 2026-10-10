package app.bus2go.android.domain.use_case

import app.bus2go.android.domain.repository.AppStateRepository
import java.security.cert.X509Certificate

class AcceptSelfSignedCertificate(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(cert: X509Certificate) {
		appStateRepository.setSelfSignedCert(cert)
	}
}
