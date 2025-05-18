package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import io.ktor.client.network.sockets.ConnectTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

class CheckIsBus2GoServer(
	private val databaseDownloadRepository: DatabaseDownloadRepository
) {
	suspend operator fun invoke(str: String): Result<Boolean> {
		return when(val res = databaseDownloadRepository.getIsBus2Go(str)){
			is Result.Error -> when(res.throwable){
				//not connected to the internet
				null -> Result.Error(null, res.message)
				is UnknownHostException -> Result.Success(false)
				is ConnectTimeoutException -> Result.Success(false)
				is ConnectException -> Result.Success(false)
				else -> Result.Error(null, null)
			}
			is Result.Success<Boolean> -> Result.Success(res.data)
		}
	}
}