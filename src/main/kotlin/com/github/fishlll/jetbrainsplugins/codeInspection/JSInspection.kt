package com.github.fishlll.jetbrainsplugins.codeInspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.jetbrains.annotations.NotNull


class JSInspection : LocalInspectionTool() {
	private val myQuickFix = ReplaceWithEqualsQuickFix()

	/**
	 * This method is overridden to provide a custom visitor
	 * that inspects expressions with relational operators '==' and '!='.
	 * The visitor must not be recursive and must be thread-safe.
	 *
	 * @param holder     object for the visitor to register problems found
	 * @param isOnTheFly true if inspection was run in non-batch mode
	 * @return non-null visitor for this inspection
	 * @see JavaElementVisitor
	 */
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return object : PsiElementVisitor() {
			/**
			 * 检测操作符是否包含 '==' 和 '!=',同时检测操作数是否是 String 类型。 检测忽略和null比较的情况。
			 * 如果满足条件，在ProblemsHolder中注册问题。(提示用户此处有问题)
			 */
			override fun visitElement(element: PsiElement){
				println("->visitElement   ${element.text}")
			}
		}
	}

	/**
	 * This class provides a solution to inspection problem expressions by manipulating the PSI tree to use 'a.equals(b)'
	 * instead of '==' or '!='.
	 */
	private class ReplaceWithEqualsQuickFix : LocalQuickFix {
		/**
		 * Returns a partially localized string for the quick fix intention.
		 * Used by the test code for this plugin.
		 *
		 * @return Quick fix short name.
		 */
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
