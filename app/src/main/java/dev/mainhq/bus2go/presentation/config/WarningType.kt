package dev.mainhq.bus2go.presentation.config

import okhttp3.CertificatePinner.Companion.sha256Hash
import java.security.cert.X509Certificate

sealed class WarningType() {
	object QuerySelfHosted: WarningType()
	data class AcceptSelfSignedCertificate(
		val cert: X509Certificate
	): WarningType() {
		val server: String
			get() = cert.subjectX500Principal.name
		val issuer: String
			get() = cert.issuerX500Principal.name
		val expires: String
			get() = cert.notAfter.toString()
		val fingerprint: String
			get() = cert.sha256Hash().toString()
	}
}
