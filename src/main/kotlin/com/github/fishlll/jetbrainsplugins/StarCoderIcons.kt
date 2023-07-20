package com.github.fishlll.jetbrainsplugins

import com.intellij.openapi.util.IconLoader

interface StarCoderIcons {
	companion object {
		val Action = IconLoader.getIcon("/icons/actionIcon.svg", StarCoderIcons::class.java)
		val WidgetEnabled = IconLoader.getIcon("/icons/widgetEnabled.svg", StarCoderIcons::class.java)
		val WidgetDisabled = IconLoader.getIcon("/icons/widgetDisabled.svg", StarCoderIcons::class.java)
		val WidgetError = IconLoader.getIcon("/icons/widgetError.svg", StarCoderIcons::class.java)
	}
}
