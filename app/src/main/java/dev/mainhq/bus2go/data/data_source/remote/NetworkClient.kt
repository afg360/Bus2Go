package dev.mainhq.bus2go.data.data_source.remote

import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


object NetworkClient {
	private val client = HttpClient(OkHttp){
		//TODO we can add an HttpResponseValidator to check for us the HTTP status codes here
		// and auto redirects
		//followRedirects = true
		engine {
			config {
				connectTimeout(15_000, TimeUnit.MILLISECONDS)
				readTimeout(15_000, TimeUnit.MILLISECONDS)
				writeTimeout(15_000, TimeUnit.MILLISECONDS)
			}
		}
	}

	private lateinit var selfHostedClient: HttpClient

	fun init(trustManager: X509TrustManager) {
		selfHostedClient = HttpClient(OkHttp){
			engine {
				config {
					connectTimeout(15_000, TimeUnit.MILLISECONDS)
					readTimeout(15_000, TimeUnit.MILLISECONDS)
					writeTimeout(15_000, TimeUnit.MILLISECONDS)
					val sslContext = SSLContext.getInstance("TLS").apply {
						init(null, arrayOf(trustManager), SecureRandom())
					}
					//FIXME add correct code
					hostnameVerifier{ _, _ -> true }
					sslSocketFactory(sslContext.socketFactory, trustManager)
				}
			}
		}
	}

	/**
	 * @return A Result.Success if no exception AND no http error occurred. Otherwise returns a
	 * Result.Error
	 * @throws IllegalArgumentException
	 * @throws ConnectTimeoutException
	 * @throws UnknownHostException
	 * @throws UnknownServiceException When TLS/SSL is not used for production code
	 * @throws SSLHandshakeException When unverified certificate is used
	 **/
	suspend fun get(url: Url, isLocal: Boolean): Result<ByteReadChannel> {
		try {
			val response = if (isLocal) selfHostedClient.get(url) {}
			else client.get(url) {}
			return when(response.status.value){
				in 100..199 -> Result.Success(response.body<ByteReadChannel>())

				in 200..299 -> Result.Success(response.body<ByteReadChannel>())

				//TODO (in case some change happened...)
				in 300..399 -> Result.Success(response.body<ByteReadChannel>())

				//TODO stricter responses...
				in 400..499 -> Result.Error(null)

				//TODO
				in 500..599 -> Result.Error(null)

				else -> Result.Error(null, "Impossible")
			}
		}
		catch (cte: ConnectTimeoutException){
			throw ConnectTimeoutException("Request has timed out", cte)
		}
	}

	//FIXME handle http responses here...
	suspend fun <T> getAndExecute(url: Url, isLocal: Boolean, block: suspend (HttpResponse) -> T): T {
		try {
			return if (isLocal) selfHostedClient.prepareGet(url).execute(block)
			else client.prepareGet(url).execute(block)
		}
		catch (ioe: IOException){
			//FIXME instead use a logger to log, and maybe do some notification or snackbar or something...
			throw NetworkException("Error when trying to query the backend server", ioe)
		}
	}

	/** A helper function dealing with formatting correctly the network call and doing basic checks */
	suspend fun <T> call(
		url: Url,
		onError: () -> Result<T>,
		onSuccess: suspend (Result.Success<ByteReadChannel>) -> T,
		networkMonitor: NetworkMonitor,
		logger: Logger?,
		tag: String,
		isLocal: Boolean
	): Result<T>{
		if (!networkMonitor.isConnected()) {
			logger?.error(tag, "Not connected")
			return Result.Error(null, "Not connected to the internet")
		}

		try{
			//if we receive an Error, then the url is wrong
			logger?.debug(tag, url.toString())
			return when(val res = get(url, isLocal)){
				is Result.Error -> onError()
				is Result.Success<ByteReadChannel> -> Result.Success(onSuccess(res))
			}
		}
		catch (iae: IllegalArgumentException){
			logger?.error(tag, "Malformed URL", iae)
			return Result.Error(null, "The URL was malformed")
		}
		catch (coe: ConnectTimeoutException){
			logger?.error(tag, "Connection timed out...", coe)
			return Result.Error(null, "Connection has timed out")
		}
		catch (uho: UnknownHostException){
			logger?.error(tag, "Unknown host", uho)
			return Result.Error(null, "The host does not exist")
		}
		catch (ce: ConnectException){
			logger?.error(tag, "Connection Exception", ce)
			return Result.Error(null, "Cannot connect to the server")
		}
		//Caused when only TLS calls can be made to a non-TLS server/endpoint
		catch (use: UnknownServiceException) {
			logger?.error(tag, "SSL/TLS is not configured on the server side or this version of the app", use)
			return Result.Error(use, "This version of the app or the server does not have SSL/TLS enabled")
		}
		//Caused when SSL certificate is wrong
		catch (he: SSLHandshakeException) {
			logger?.error(tag, "Server SSL Certificate is invalid")
			return Result.Error(he, "Server SSL Certificate is invalid. You may need to manually add it to your device if you are self hosting the server")
		}
		catch (ioe: IOException){
			logger?.error(tag, "Unknown IOException occurred", ioe)
			return Result.Error(ioe, null)
		}
	}

	//TODO websockets handling
}