package dev.mainhq.bus2go.data.data_source.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dev.mainhq.bus2go.domain.core.Logger

class NetworkMonitor private constructor(context: Context, private val logger: Logger?) {
	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	companion object{
		private const val TAG = "NETWORK_MONITOR"
		private var INSTANCE: NetworkMonitor? = null

		//TODO need to make it thread safe even if init synchronously?
		fun getInstance(context: Context, logger: Logger?): NetworkMonitor{
			return INSTANCE ?: NetworkMonitor(context, logger).also { INSTANCE = it }
		}
	}

	fun isConnected(): Boolean {
		val network = connectivityManager.activeNetwork ?: return false
		val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

		//check if connected to internet
		if (connectivityManager.activeNetwork == null) return false
		return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

		//TODO the below is for constant monitoring, should be moved to a different function
		/*
		val networkRequest = NetworkRequest.Builder()
			.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
			.build()

		val networkCallback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				super.onAvailable(network)
				logger?.debug(TAG, "Available...")
			}

			override fun onLost(network: Network) {
				super.onLost(network)
				logger?.debug(TAG, "Lost connection to network")
			}

			override fun onUnavailable() {
				super.onUnavailable()
				logger?.debug(TAG, "Networks are unavailable")
			}
		}
		connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
		 */
	}
}