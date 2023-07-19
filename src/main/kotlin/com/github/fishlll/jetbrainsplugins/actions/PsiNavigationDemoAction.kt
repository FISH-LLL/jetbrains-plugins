package com.github.fishlll.jetbrainsplugins.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class PsiNavigationDemoAction: AnAction(){

	override fun actionPerformed(e: AnActionEvent) {

		//当前获取焦点的 editor 实例
		val editor = e.getData(CommonDataKeys.EDITOR)
		//editor 对应选择的 psiFile 对象
		val psiFile = e.getData(CommonDataKeys.PSI_FILE)
		if (editor == null || psiFile == null) {
			return
		}

		val offset = editor.caretModel.offset
		val element = psiFile.findElementAt(offset) ?: return
		println(element)
		var info = "光标所在元素: ${element}" + "\n"
		val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
		println(containingMethod)
	}

	override fun update(e: AnActionEvent) {
		super.update(e)

	}
}