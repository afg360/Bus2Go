package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.presentation.config.ServerType
import io.ktor.client.network.sockets.ConnectTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException

class CheckIsBus2GoServer(
	private val databaseDownloadRepository: DatabaseDownloadRepository
) {
	/**
	 * @return If any network failure, returns an error. If there are none,
	 * if the server queried is a valid bus2go server, returns true, else false.
	 * */
	suspend operator fun invoke(str: String, serverType: ServerType): Result<Boolean> {
		//TODO handle the query differently if
		when(serverType) {
			ServerType.SELF_HOSTED -> {
				//TODO right network call
				return when(val res = databaseDownloadRepository.getIsBus2Go(str, true)){
					is Result.Error -> when(res.throwable){
						//not connected to the internet
						null -> Result.Error(null, res.message)
						is UnknownHostException -> Result.Success(false)
						is ConnectTimeoutException -> Result.Success(false)
						is ConnectException -> Result.Success(false)
						//when using no encryption when clear traffic is off
						is UnknownServiceException -> Result.Error(res.throwable, res.message)
						//Prompt to manually enter the self-signed certificate if there are any (open a dialog and warn the user)
						is SSLHandshakeException -> Result.Error(res.throwable, res.message)
						else -> Result.Error(null, null)
					}
					is Result.Success<Boolean> -> Result.Success(res.data)
				}
			}
			ServerType.WEB -> {
				return when(val res = databaseDownloadRepository.getIsBus2Go(str, false)){
					is Result.Error -> when(res.throwable){
						//not connected to the internet
						null -> Result.Error(null, res.message)
						is UnknownHostException -> Result.Success(false)
						is ConnectTimeoutException -> Result.Success(false)
						is ConnectException -> Result.Success(false)
						is UnknownServiceException -> Result.Error(res.throwable, res.message)
						is SSLHandshakeException -> Result.Error(res.throwable, res.message)
						else -> Result.Error(null, null)
					}
					is Result.Success<Boolean> -> Result.Success(res.data)
				}
			}
		}
	}


}