package dev.mainhq.bus2go.domain.exceptions

import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class ExpiredCertificateException(
	val certificate: X509Certificate,
): CertificateException("Certificate is expired: ${certificate.subjectX500Principal.name}")