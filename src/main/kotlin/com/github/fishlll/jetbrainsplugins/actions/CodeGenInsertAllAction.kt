package com.github.fishlll.jetbrainsplugins.actions

import com.github.fishlll.jetbrainsplugins.StarCoderWidget
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import java.util.*

class CodeGenInsertAllAction : AnAction() {
	override fun actionPerformed(e: AnActionEvent) {
		val editor = e.getData(CommonDataKeys.EDITOR)
		val caret = e.getData(CommonDataKeys.CARET)
		val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
		if (!performAction(editor, caret, file)) {
			// TODO log?
		}
	}

	override fun update(e: AnActionEvent) {
		// Only allow this if there are hints in the userdata.
		val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
		val hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION)
		e.presentation.isEnabledAndVisible = hints != null && hints.size > 0
	}

	companion object {
		fun performAction(editor: Editor?, caret: Caret?, file: VirtualFile?): Boolean {
			if (file == null) return false
			val hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION)
			if (hints == null || hints.size == 0) return false
			val starCoderPos = file.getUserData(StarCoderWidget.STAR_CODER_POSITION)
			val lastPosition = starCoderPos ?: 0
			if (caret == null || caret.offset != lastPosition) return false
			val insertTextJoiner = StringJoiner("")
			for (hint in hints) {
				insertTextJoiner.add(hint)
			}
			file.putUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION, null)
			val insertText = insertTextJoiner.toString()
			WriteCommandAction.runWriteCommandAction(editor!!.project, "StarCoder Insert", null, {
				editor.document.insertString(lastPosition, insertText)
				editor.caretModel.moveToOffset(lastPosition + insertText.length)
			})
			return true
		}
	}
}
