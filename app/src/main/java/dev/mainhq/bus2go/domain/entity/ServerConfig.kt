package dev.mainhq.bus2go.domain.entity

import androidx.core.text.isDigitsOnly

sealed class ServerConfig {
	abstract val data: String
	data class DomainName(override val data: String): ServerConfig()
	data class IpAddress(override val data: String): ServerConfig()

	companion object {
		fun build(string: String): ServerConfig? {
			val elems = string.split(".")
			if (elems.any { it.isBlank() }) return null
			//may be an ip address
			if (elems.size == 4){
				//check if it is a domain name
				if (string.length > 15) return DomainName(string)
				//we may have an ip address
				else {
					//verify if each elem is a number between 0 and 255, and if all numerical data
					val isIp = elems.filter { it.isDigitsOnly() }
						.map{ it.toInt() }
						.filter { it in 0..255 }
						.size == 4
					return if (isIp) IpAddress(string) else null
				}
			}
			//must be a domain name (since we must have foo.bar
			//FIXME verify the domain name exists...
			else if (elems.size > 1 && !elems.any { it.isDigitsOnly() }) return DomainName(string)
			else return null
		}
	}

}