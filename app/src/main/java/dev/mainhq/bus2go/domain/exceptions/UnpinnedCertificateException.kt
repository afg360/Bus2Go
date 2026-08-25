package dev.mainhq.bus2go.domain.exceptions

import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class UnpinnedCertificateException(
	val certificate: X509Certificate,
) : CertificateException("Certificate not pinned: ${certificate.subjectX500Principal.name}")
