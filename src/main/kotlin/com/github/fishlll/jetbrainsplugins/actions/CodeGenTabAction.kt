package com.github.fishlll.jetbrainsplugins.actions

import com.github.fishlll.jetbrainsplugins.settings.StarCoderSettings
import com.github.fishlll.jetbrainsplugins.settings.TabActionOption
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

class CodeGenTabAction(protected val myOriginalHandler: EditorActionHandler) : EditorWriteActionHandler() {
	override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
		if (!insertCodeSuggestion(editor, caret, dataContext)) {
			myOriginalHandler.execute(editor, caret, dataContext)
		}
	}

	private fun insertCodeSuggestion(editor: Editor, caret: Caret?, dataContext: DataContext): Boolean {

		val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
		val tabActionOption = StarCoderSettings.instance.tabActionOption
		return when (tabActionOption) {
			//插入所有建议
			TabActionOption.ALL -> CodeGenInsertAllAction.performAction(editor, caret, file)
			//插入单行建议
			TabActionOption.SINGLE -> CodeGenInsertLineAction.performAction(editor, caret, file)
			else -> false
		}
	}
}
