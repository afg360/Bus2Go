package dev.mainhq.bus2go.data.data_source.local

import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.utils.toTime
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.time.Instant
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

//TODO Use an EncryptFile from Jetpack to encrypt the KeyStore file
/** Class used to properly store self-signed certificates when self-hosting a server. */
class LocalKeyStore(
	filesDir: File,
) {

	private val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
	private val keyStoreFile = File(filesDir, FILENAME)
	private val mutex = Mutex()
	private val memory = ConcurrentHashMap<String, X509Certificate>()

	private companion object {
		private const val CERT_ENTRY = "self-hosted-home-ca"
		private const val FILENAME = "keystore.p12"
	}

	init {
		if (keyStoreFile.exists()) {
			keyStoreFile.inputStream().use { stream ->
				keyStore.load(stream, null)
				(keyStore.getCertificate(CERT_ENTRY) as? X509Certificate)?.let {
					memory.compute(CERT_ENTRY) { _, _ -> it }
				}
			}
		}
		else {
			keyStore.load(null, null)
		}
	}

	suspend fun saveNewCertificate(cert: X509Certificate) {
		mutex.withLock {
			keyStore.setCertificateEntry(CERT_ENTRY, cert)
			keyStoreFile.outputStream().use { stream ->
				keyStore.store(stream, null)
			}
		}
		memory.compute(CERT_ENTRY) { _, _ -> cert }
	}

	fun isExpired(cert: X509Certificate): Boolean {
		//check if the dates are respected
		return cert.notAfter.toInstant().toTime() < Time.now()
	}

	fun isTrusted(cert: X509Certificate): Boolean {
		//need to compare the contents
		return memory[CERT_ENTRY]?.encoded?.contentEquals(cert.encoded) ?: false
	}

}