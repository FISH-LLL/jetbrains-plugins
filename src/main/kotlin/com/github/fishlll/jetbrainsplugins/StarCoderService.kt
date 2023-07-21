package com.github.fishlll.jetbrainsplugins

import com.github.fishlll.jetbrainsplugins.settings.StarCoderSettings
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.IOException

class StarCoderService {
	var status = 200
		private set

	fun getCodeCompletionHints(editorContents: CharSequence, cursorPosition: Int): Array<String>? {
		val settings = StarCoderSettings.instance
		if (!settings.isSaytEnabled) return null

		// TODO Notification banner?
		if (StringUtils.isEmpty(settings.apiToken)) {
			Notifications.Bus.notify(
				Notification(
					"StarCoder",
					"StarCoder",
					"StarCoder API token is required.",
					NotificationType.WARNING
				)
			)
			return null
		}
		val contents = editorContents.toString()
		if (contents.contains(PREFIX_TAG) || contents.contains(SUFFIX_TAG) || contents.contains(MIDDLE_TAG) || contents.contains(
				END_TAG
			)
		) return null
		val prefix = contents.substring(0, cursorPosition)
		val suffix = contents.substring(cursorPosition, editorContents.length)
		val starCoderPrompt = generateFIMPrompt(prefix, suffix)
		val httpPost = buildApiPost(settings, starCoderPrompt)
		println("->StarCoderService.getCodeCompletionHints Calling API: $cursorPosition")
		val generatedText = getApiResponse(httpPost)
		var suggestionList: Array<String>? = null
		if (generatedText.contains(MIDDLE_TAG)) {
			val parts = generatedText.split(MIDDLE_TAG.toRegex()).dropLastWhile { it.isEmpty() }
				.toTypedArray()
			if (parts.size > 1) {
				suggestionList = StringUtils.splitPreserveAllTokens(parts[1], "\n")
				if (suggestionList.size == 1 && suggestionList[0].trim { it <= ' ' }.length == 0) return null
				if (suggestionList.size > 1) {
					for (i in suggestionList.indices) {
						val sb = StringBuilder(suggestionList[i])
						sb.append("\n")
						suggestionList[i] = sb.toString()
					}
				}
			}
		}
		return suggestionList
	}

	private fun generateFIMPrompt(prefix: String, suffix: String): String {
		return PREFIX_TAG + prefix + SUFFIX_TAG + suffix + MIDDLE_TAG
	}

	private fun buildApiPost(settings: StarCoderSettings, starCoderPrompt: String): HttpPost {
		val apiURL = settings.apiURL
		val bearerToken = settings.apiToken
		val temperature = settings.temperature
		val maxNewTokens = settings.maxNewTokens
		val topP = settings.topP
		val repetitionPenalty = settings.repetitionPenalty
		val httpPost = HttpPost(apiURL)
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
		val httpBody = JsonObject()
		httpBody.addProperty("inputs", starCoderPrompt)
		val parameters = JsonObject()
		parameters.addProperty("temperature", temperature)
		parameters.addProperty("max_new_tokens", maxNewTokens)
		parameters.addProperty("top_p", topP)
		parameters.addProperty("repetition_penalty", repetitionPenalty)
		httpBody.add("parameters", parameters)
		val requestEntity = StringEntity(httpBody.toString(), ContentType.APPLICATION_JSON)
		httpPost.entity = requestEntity
		println("->buildApiPost ${apiURL}\nentity:${httpBody}")
		return httpPost
	}

	private fun getApiResponse(httpPost: HttpPost): String {
		var responseText = ""
		try {
			val httpClient = HttpClients.createDefault()
			val response: HttpResponse = httpClient.execute(httpPost)

			// Check the response status code
			val oldStatusCode = status
			status = response.statusLine.statusCode
			if (status != oldStatusCode) {
				// Update the widget based on the new status code
				for (openProject in ProjectManager.getInstance().openProjects) {
					WindowManager.getInstance().getStatusBar(openProject).updateWidget(StarCoderWidget.ID)
				}
			}
			if (status != 200) {
				return responseText
			}
			val gson = Gson()
			val responseBody = EntityUtils.toString(response.entity)
			val responseArray = gson.fromJson(responseBody, JsonArray::class.java)
			val generatedText: String
			if (responseArray.size() > 0) {
				val responseObject = responseArray[0].asJsonObject
				if (responseObject["generated_text"] != null) {
					generatedText = responseObject["generated_text"].asString
					responseText = generatedText.replace(END_TAG, "")
				}
			}
			httpClient.close()
		} catch (e: IOException) {
			// TODO log exception
		}

		println("->getApiResponse: \n${responseText}")

		return responseText
	}

	fun replacementSuggestion(prompt: String): String {
		// Default to returning the same text.
		var replacement = prompt
		val settings = StarCoderSettings.instance
		if (StringUtils.isEmpty(settings.apiToken)) {
			Notifications.Bus.notify(
				Notification(
					"StarCoder",
					"StarCoder",
					"StarCoder API token is required.",
					NotificationType.WARNING
				)
			)
			return replacement
		}
		val httpPost = buildApiPost(settings, prompt)
		val generatedText = getApiResponse(httpPost)
		if (!StringUtils.isEmpty(generatedText)) {
			replacement = generatedText
		}
		return replacement
	}

	companion object {
		// TODO: SantaCoder uses - rather than _
		private const val PREFIX_TAG = "<fim_prefix>"
		private const val SUFFIX_TAG = "<fim_suffix>"
		private const val MIDDLE_TAG = "<fim_middle>"
		private const val END_TAG = "<|endoftext|>"
	}
}
