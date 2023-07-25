package com.github.fishlll.jetbrainsplugins.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.jetbrains.annotations.NotNull
import com.intellij.lang.javascript.psi.*


class JSInspection : LocalInspectionTool() {
	private val myQuickFix = ReplaceWithEqualsQuickFix()

	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return object : PsiElementVisitor() {

			override fun visitElement(element: PsiElement){
				//val classLoader = element::class.java.classLoader
				//val classPath = classLoader.getResource(element::class.java.name.replace('.', '/') + ".class")?.toString() ?: ""
				println("->visitElement(${element::class.simpleName}) ${element.text}")
				when (element) {
					is JSObjectLiteralExpression -> {

					}
					else -> {

					}
				}


			}
		}
	}

	private class ReplaceWithEqualsQuickFix : LocalQuickFix {

		override fun getName(): String {
			return InspectionBundle.ourInstance.message("inspection.comparing.string.references.use.quickfix") ?: ""
		}

		/**
		 * 操作 PSI语法树, 用 'a.equals(b)' 替换 'a==b'。 '!a.equals(b)' 替换 'a!=b'
		 *
		 * @param project    The project that contains the file being edited.
		 * @param descriptor A problem found by this inspection.
		 */
		override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
			val binaryExpression = descriptor.psiElement as PsiBinaryExpression
			val opSign = binaryExpression.operationTokenType
			val lExpr = binaryExpression.lOperand
			val rExpr = binaryExpression.rOperand ?: return
			val factory = JavaPsiFacade.getInstance(project).elementFactory
			val equalsCall = factory.createExpressionFromText("a.equals(b)", null) as PsiMethodCallExpression
			equalsCall.methodExpression.qualifierExpression!!.replace(lExpr)
			equalsCall.argumentList.expressions[0].replace(rExpr)
			val result = binaryExpression.replace(equalsCall) as PsiExpression
			if (opSign === JavaTokenType.NE) {
				val negation = factory.createExpressionFromText("!a", null) as PsiPrefixExpression
				negation.operand!!.replace(result)
				result.replace(negation)
			}
		}

		override fun getFamilyName(): String {
			return name
		}
	}
}
