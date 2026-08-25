package dev.mainhq.bus2go.data.data_source.remote

import dev.mainhq.bus2go.data.data_source.local.LocalKeyStore
import dev.mainhq.bus2go.domain.exceptions.ExpiredCertificateException
import dev.mainhq.bus2go.domain.exceptions.UnpinnedCertificateException
import java.io.File
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class CustomTrustManager private constructor(
	private val systemTrustManager: X509TrustManager,
	private val keyStore: LocalKeyStore,
): X509TrustManager {

	companion object {
		fun build(keyStore: LocalKeyStore): CustomTrustManager {
			return CustomTrustManager(
				systemTrustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
					.apply {
						init(null as KeyStore?)
					}
					.trustManagers
					.filterIsInstance<X509TrustManager>()
					.first(),
				keyStore = keyStore
			)
		}
	}

	/**
	 * Will not be used since we are a client, but uses default nonetheless
	 * @throws IllegalArgumentException if null or zero-length chain is passed in for the
	 * chain parameter or if null or zero-length string is passed in for the authType parameter
	 * @throws CertificateException if the certificate chain is not trusted by
	 * this TrustManager
	 */
	override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
		if (chain == null || authType == null || chain.isEmpty()) {
			throw IllegalArgumentException()
		}
		systemTrustManager.checkClientTrusted(chain, authType)
	}

	/**
	 * @throws IllegalArgumentException if null or zero-length chain is passed in for the
	 * chain parameter or if null or zero-length string is passed in for the authType parameter
	 * @throws UnpinnedCertificateException if the certificate chain is not trusted by
	 * @throws ExpiredCertificateException if the certificate coming from the server is expired
	 * this TrustManager
	 */
	override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
		if (chain == null || authType == null || chain.isEmpty()) {
			throw IllegalArgumentException()
		}
		try {
			systemTrustManager.checkServerTrusted(chain, authType)
		}
		catch (ce: CertificateException) {
			//TODO may need to have tighter security checks here
			val serverCert = chain[0]
			if (keyStore.isExpired(serverCert)) {
				throw ExpiredCertificateException(serverCert)
			}
			if (!keyStore.isTrusted(serverCert)) {
				throw UnpinnedCertificateException(serverCert)
			}
		}
	}

	override fun getAcceptedIssuers(): Array<out X509Certificate> = systemTrustManager.acceptedIssuers

}
