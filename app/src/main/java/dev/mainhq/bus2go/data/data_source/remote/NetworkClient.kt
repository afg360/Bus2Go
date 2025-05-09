package dev.mainhq.bus2go.data.data_source.remote

import dev.mainhq.bus2go.domain.exceptions.NetworkException
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.http.isSuccess
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {
	private val client = HttpClient(OkHttp){
		engine {
			config {
				connectTimeout(15_000, TimeUnit.MILLISECONDS)
				readTimeout(15_000, TimeUnit.MILLISECONDS)
				writeTimeout(15_000, TimeUnit.MILLISECONDS)
			}
		}
	}

	/** @throws NetworkException When the get call is not successful. */
	suspend fun get(url: Url): HttpResponse {
		try {
			val response = client.get(url) {}
			if (response.status.isSuccess()) return response
			else throw NetworkException(
				"Response code: ${response.status.value}\n" +
						"Description: ${response.status.description}"
			)
		}
		catch (ioe: IOException){
			throw NetworkException("Error when trying to query the backend server", ioe)
		}
	}

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