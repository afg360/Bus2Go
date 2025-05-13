package dev.mainhq.bus2go.data.data_source.remote

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
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.net.UnknownHostException

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

	/**
	 * @return A Result.Success if no exception AND no http error occurred. Otherwise returns a
	 * Result.Error
	 * @throws IllegalArgumentException
	 * @throws ConnectTimeoutException
	 * @throws UnknownHostException
	 **/
	suspend fun get(url: Url): Result<ByteReadChannel> {
		try {
			val response = client.get(url) {}
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
	suspend fun <T> getAndExecute(url: Url, block: suspend (HttpResponse) -> T): T {
		try {
			return client.prepareGet(url).execute(block)
		}
		catch (ioe: IOException){
			//FIXME instead use a logger to log, and maybe do some notification or snackbar or something...
			throw NetworkException("Error when trying to query the backend server", ioe)
		}
	}

	//TODO websockets handling
}