package com.github.fishlll.jetbrainsplugins

import com.intellij.codeInsight.daemon.impl.HintRenderer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.TextAttributes

class CodeGenHintRenderer(text: String?) : HintRenderer(text) {
	override fun getTextAttributes(editor: Editor): TextAttributes? {
		// TODO custom color schemes?
		val newAttributes = TextAttributes()
		newAttributes.copyFrom(editor.colorsScheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT))
		return newAttributes
	}
}
