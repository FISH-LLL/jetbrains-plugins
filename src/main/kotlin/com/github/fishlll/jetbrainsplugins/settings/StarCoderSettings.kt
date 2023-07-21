package com.github.fishlll.jetbrainsplugins.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jdom.Element
import java.lang.Boolean
import kotlin.String

@State(name = "StarCoderSettings", storages = [Storage("starcoder_settings.xml")])
class StarCoderSettings : PersistentStateComponent<Element?> {
	var isSaytEnabled = true
	var apiURL = "https://api-inference.huggingface.co/models/bigcode/starcoder"
	@JvmField
    var tabActionOption = TabActionOption.ALL
	var temperature = 1.0f
		private set
	var maxNewTokens = 1024
		private set
	var topP = 0.9f
		private set
	var repetitionPenalty = 1.2f
		private set

	override fun getState(): Element? {
		val state = Element(SETTINGS_TAG)
		state.setAttribute(API_URL_TAG, apiURL)
		state.setAttribute(SAYT_TAG, Boolean.toString(isSaytEnabled))
		state.setAttribute(TAB_ACTION_TAG, tabActionOption.name)
		state.setAttribute(TEMPERATURE_TAG, temperature.toString())
		state.setAttribute(MAX_NEW_TOKENS_TAG, maxNewTokens.toString())
		state.setAttribute(TOP_P_TAG, topP.toString())
		state.setAttribute(REPEAT_PENALTY_TAG, repetitionPenalty.toString())
		return state
	}

	override fun loadState(state: Element) {
		if (state.getAttributeValue(API_URL_TAG) != null) {
			apiURL = state.getAttributeValue(API_URL_TAG)
		}
		if (state.getAttributeValue(SAYT_TAG) != null) {
			isSaytEnabled = Boolean.parseBoolean(state.getAttributeValue(SAYT_TAG))
		}
		if (state.getAttributeValue(TAB_ACTION_TAG) != null) {
			tabActionOption = TabActionOption.valueOf(state.getAttributeValue(TAB_ACTION_TAG))
		}
		if (state.getAttributeValue(TEMPERATURE_TAG) != null) {
			setTemperature(state.getAttributeValue(TEMPERATURE_TAG))
		}
		if (state.getAttributeValue(MAX_NEW_TOKENS_TAG) != null) {
			setMaxNewTokens(state.getAttributeValue(MAX_NEW_TOKENS_TAG))
		}
		if (state.getAttributeValue(TOP_P_TAG) != null) {
			setTopP(state.getAttributeValue(TOP_P_TAG))
		}
		if (state.getAttributeValue(REPEAT_PENALTY_TAG) != null) {
			setRepetitionPenalty(state.getAttributeValue(REPEAT_PENALTY_TAG))
		}
	}

	fun toggleSaytEnabled() {
		isSaytEnabled = !isSaytEnabled
	}

	var apiToken: String?
		get() {
			val credentials = PasswordSafe.instance.get(CREDENTIAL_ATTRIBUTES)
			return if (credentials != null) credentials.getPasswordAsString() else ""
		}
		set(apiToken) {
			PasswordSafe.instance.set(CREDENTIAL_ATTRIBUTES, Credentials(null, apiToken))
		}

	fun setTemperature(temperature: String) {
		this.temperature = temperature.toFloat()
	}

	fun setMaxNewTokens(maxNewTokens: String) {
		this.maxNewTokens = maxNewTokens.toInt()
	}

	fun setTopP(topP: String) {
		this.topP = topP.toFloat()
	}

	fun setRepetitionPenalty(repetitionPenalty: String) {
		this.repetitionPenalty = repetitionPenalty.toFloat()
	}

	companion object {
		const val SETTINGS_TAG = "StarCoderSettings"
		private const val API_URL_TAG = "API_URL"
		private val CREDENTIAL_ATTRIBUTES =
			CredentialAttributes(StarCoderSettings::class.java.name, "STARCODER_BEARER_TOKEN")
		private const val SAYT_TAG = "SAYT_ENABLED"
		private const val TAB_ACTION_TAG = "TAB_ACTION"
		private const val TEMPERATURE_TAG = "TEMPERATURE"
		private const val MAX_NEW_TOKENS_TAG = "MAX_NEW_TOKENS"
		private const val TOP_P_TAG = "TOP_P"
		private const val REPEAT_PENALTY_TAG = "REPEAT_PENALTY"
		private val starCoderSettingsInstance = StarCoderSettings()
		@JvmStatic
        val instance: StarCoderSettings
			get() = if (ApplicationManager.getApplication() == null) {
				starCoderSettingsInstance
			} else ApplicationManager.getApplication().getService(
				StarCoderSettings::class.java
			) ?: starCoderSettingsInstance
	}
}
