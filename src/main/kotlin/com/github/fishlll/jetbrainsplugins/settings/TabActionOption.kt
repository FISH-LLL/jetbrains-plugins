package com.github.fishlll.jetbrainsplugins.settings

enum class TabActionOption(private val description: String) {
	// TODO add action class here?
	ALL("All suggestions"),
	SINGLE("Single line at a time"),
	DISABLED("None");

	override fun toString(): String {
		return description
	}
}
