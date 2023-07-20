package com.github.fishlll.jetbrainsplugins.actions

import com.github.fishlll.jetbrainsplugins.StarCoderService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import org.apache.commons.lang3.StringUtils

class CodeGenPromptAction : AnAction() {
	override fun actionPerformed(e: AnActionEvent) {
		val focusedEditor = e.dataContext.getData(CommonDataKeys.EDITOR) ?: return
		val selectionStart = focusedEditor.selectionModel.selectionStart
		val selectionEnd = focusedEditor.selectionModel.selectionEnd
		val selectedText = focusedEditor.caretModel.currentCaret.selectedText
		if (StringUtils.isEmpty(selectedText))
			return

		val starCoder = ApplicationManager.getApplication().getService(
			StarCoderService::class.java
		)
		val replacementSuggestion = starCoder.replacementSuggestion(selectedText!!)
		WriteCommandAction.runWriteCommandAction(
			focusedEditor.project,
			"StarCoder Insert",
			null,
			{ focusedEditor.document.replaceString(selectionStart, selectionEnd, replacementSuggestion) })
	}

	override fun update(e: AnActionEvent) {
		// Only show the action if there is selected text in the editor
		var selection = ""
		val focusedEditor = e.dataContext.getData(CommonDataKeys.EDITOR)
		if (focusedEditor != null) {
			selection = focusedEditor!!.caretModel.currentCaret.selectedText ?: ""
		}
		e.presentation.isEnabledAndVisible = !StringUtils.isEmpty(selection)
	}
}
