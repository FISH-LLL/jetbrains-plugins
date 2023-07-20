package com.github.fishlll.jetbrainsplugins

enum class StarCoderStatus(val code: Int, val displayValue: String) {
	UNKNOWN(0, "Unknown"),
	OK(200, "OK"),
	BAD_REQUEST(400, "Bad request/token"),
	NOT_FOUND(404, "404 Not found"),
	TOO_MANY_REQUESTS(429, "Too many requests right now"),
	BAD_GATEWAY(502, "Bad gateway"),
	UNAVAILABLE(503, "Service unavailable");

	companion object {
		fun getStatusByCode(code: Int): StarCoderStatus {
			return values().firstOrNull { it.code == code } ?: UNKNOWN
		}
	}
}
