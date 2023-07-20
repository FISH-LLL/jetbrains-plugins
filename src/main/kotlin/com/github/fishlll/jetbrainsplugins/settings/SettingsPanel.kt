package com.github.fishlll.jetbrainsplugins.settings

import com.intellij.ui.EnumComboBoxModel
import java.awt.event.ActionEvent
import javax.swing.*

class SettingsPanel {
	val panel: JPanel? = null
	private val apiUrlTextField: JTextField? = null
	private val API: JPanel? = null
	private val Parameters: JPanel? = null
	private val bearerTokenTextField: JTextField? = null
	private val temperatureTextField: JTextField? = null
	private val maxNewTokensTextField: JTextField? = null
	private val topPTextField: JTextField? = null
	private val repetitionTextField: JTextField? = null
	private val enableSAYTCheckBox: JCheckBox? = null
	private val Settings: JPanel? = null
	private val ParamOuter: JPanel? = null
	private val TabActionPanel: JPanel? = null
	private val tabActionComboBox: JComboBox<TabActionOption>? = null
	private val tabActionLabel: JLabel? = null

	init {
		tabActionComboBox!!.model = EnumComboBoxModel(TabActionOption::class.java)
		enableSAYTCheckBox!!.addActionListener { e: ActionEvent? ->
			tabActionLabel!!.isEnabled = enableSAYTCheckBox.isSelected
			tabActionComboBox.setEnabled(enableSAYTCheckBox.isSelected)
		}
	}

	var apiUrl: String?
		get() = apiUrlTextField!!.text
		set(apiUrl) {
			apiUrlTextField!!.text = apiUrl
		}
	var apiToken: String?
		get() = bearerTokenTextField!!.text
		set(bearerToken) {
			bearerTokenTextField!!.text = bearerToken
		}
	var temperature: String?
		get() = temperatureTextField!!.text
		set(temperature) {
			temperatureTextField!!.text = temperature
		}
	var maxNewTokens: String?
		get() = maxNewTokensTextField!!.text
		set(maxNewTokens) {
			maxNewTokensTextField!!.text = maxNewTokens
		}
	var topP: String?
		get() = topPTextField!!.text
		set(topP) {
			topPTextField!!.text = topP
		}
	var repetition: String?
		get() = repetitionTextField!!.text
		set(repetition) {
			repetitionTextField!!.text = repetition
		}

	fun getEnableSAYTCheckBox(): Boolean {
		return enableSAYTCheckBox!!.isSelected
	}

	fun setEnableSAYTCheckBox(enableSAYT: Boolean) {
		enableSAYTCheckBox!!.isSelected = enableSAYT
		tabActionLabel!!.isEnabled = enableSAYT
		tabActionComboBox!!.isEnabled = enableSAYT
	}

	var tabActionOption: TabActionOption?
		get() = tabActionComboBox!!.model.selectedItem as TabActionOption
		set(option) {
			tabActionComboBox!!.model.selectedItem = option
		}
}
