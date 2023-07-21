package com.github.fishlll.jetbrainsplugins

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

class StarCoderWidgetFactory : StatusBarEditorBasedWidgetFactory() {
	override fun getId(): @NonNls String {
		return StarCoderWidget.ID
	}

	override fun getDisplayName(): @Nls String {
		return "StarCoder"
	}

	override fun createWidget(project: Project): StatusBarWidget {
		return StarCoderWidget(project)
	}

	override fun disposeWidget(widget: StatusBarWidget) {
		Disposer.dispose(widget)
	}
}
