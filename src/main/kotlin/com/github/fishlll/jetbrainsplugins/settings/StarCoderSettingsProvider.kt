package com.github.fishlll.jetbrainsplugins.settings

import com.github.fishlll.jetbrainsplugins.StarCoderWidget
import com.intellij.application.options.editor.EditorOptionsProvider
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.intellij.openapi.wm.WindowManager
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent

import com.github.fishlll.jetbrainsplugins.settings.SettingsPanel

class StarCoderSettingsProvider : EditorOptionsProvider {
	private var settingsPanel: SettingsPanel? = null
	override fun getId(): @NonNls String {
		return "StarCoder.Settings"
	}

	override fun getDisplayName(): @ConfigurableName String? {
		return "StarCoder"
	}

	override fun createComponent(): JComponent? {
		if (settingsPanel == null) {
			settingsPanel = SettingsPanel()
		}
		return settingsPanel!!.panel
	}

	override fun isModified(): Boolean {
		val savedSettings: StarCoderSettings = StarCoderSettings.instance
		return (savedSettings.apiURL != settingsPanel!!.apiUrl ||
				savedSettings.apiToken != settingsPanel!!.apiToken ||
				savedSettings.tabActionOption != settingsPanel!!.tabActionOption) ||
				savedSettings.isSaytEnabled != settingsPanel!!.enableSAYTCheckBox ||
				savedSettings.temperature != (settingsPanel!!.temperature?.toFloat() ?: 0.0) ||
				savedSettings.maxNewTokens != settingsPanel!!.maxNewTokens!!.toInt() ||
				savedSettings.topP != (settingsPanel!!.topP?.toFloat() ?: 0.0) ||
				savedSettings.repetitionPenalty != (settingsPanel!!.repetition?.toFloat() ?: 0.0)
	}

	@Throws(ConfigurationException::class)
	override fun apply() {
		val savedSettings: StarCoderSettings = StarCoderSettings.instance
		savedSettings.apiURL = settingsPanel!!.apiUrl ?: ""
		savedSettings.apiToken = settingsPanel!!.apiToken
		savedSettings.isSaytEnabled = settingsPanel!!.enableSAYTCheckBox
		savedSettings.tabActionOption = settingsPanel!!.tabActionOption!!
		savedSettings.setTemperature(settingsPanel!!.temperature ?: "")
		savedSettings.setMaxNewTokens(settingsPanel!!.maxNewTokens ?: "")
		savedSettings.setTopP(settingsPanel!!.topP ?: "")
		savedSettings.setRepetitionPenalty(settingsPanel!!.repetition ?: "")

		// Update the widget
		for (openProject in ProjectManager.getInstance().openProjects) {
			WindowManager.getInstance().getStatusBar(openProject).updateWidget(StarCoderWidget.ID)
		}
	}

	override fun reset() {
		val savedSettings: StarCoderSettings = StarCoderSettings.instance
		settingsPanel!!.apiUrl = savedSettings.apiURL
		settingsPanel!!.apiToken = savedSettings.apiToken
		settingsPanel!!.enableSAYTCheckBox = savedSettings.isSaytEnabled
		settingsPanel!!.tabActionOption = savedSettings.tabActionOption
		settingsPanel!!.temperature = savedSettings.temperature.toString()
		settingsPanel!!.maxNewTokens = savedSettings.maxNewTokens.toString()
		settingsPanel!!.topP = savedSettings.topP.toString()
		settingsPanel!!.repetition = savedSettings.repetitionPenalty.toString()
	}
}
