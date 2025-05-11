package dev.mainhq.bus2go.domain.entity

sealed class UrlChecker {
	abstract val data: String
	data class DomainName(override val data: String): UrlChecker()
	data class IpAddress(override val data: String): UrlChecker()

	companion object {
		fun check(string: String): UrlChecker? {
			val elems = string.split(".")
			if (elems.any { it.isBlank() }) return null
			//may be an ip address
			if (elems.size == 4){
				//check if it is a domain name
				if (string.length > 15) {
					//in all elems there must be @ least 1 letter
					return if (elems.last().length >= 2 && elems.all{elem -> elem.any { it.isLetter()} } )
						DomainName(string)
					else null
				}
				//we may have an ip address
				else {
					//verify if each elem is a number between 0 and 255, and if all numerical data
					val isIp = elems.filter { elem -> elem.all{ it.isDigit() } }
						.map{ it.toInt() }
						.filter { it in 0..255 }
						.size == 4
					return if (isIp) IpAddress(string)
					else if (elems.last().length >= 2 && elems.all{elem -> elem.any { it.isLetter()} } )
						DomainName(string)
					else null
				}
			}
			//must be a domain name (since we must have foo.bar)
			else if (elems.size > 1 && elems.last().length >= 2 && elems.all{elem -> !elem.all { it.isDigit() } } )
				return DomainName(string)
			else return null
		}
	}

}