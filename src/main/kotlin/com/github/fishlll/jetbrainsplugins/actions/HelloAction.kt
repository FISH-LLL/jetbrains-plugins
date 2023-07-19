package com.github.fishlll.jetbrainsplugins.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages


class HelloAction : DumbAwareAction(){
	override fun actionPerformed(e: AnActionEvent) {
		val project = e.getData(PlatformDataKeys.PROJECT)
		Messages.showMessageDialog(project, "Hello from Kotlin!", "Greeting", Messages.getInformationIcon())
	}
}