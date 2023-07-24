// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.fishlll.jetbrainsplugins.codeInspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*

/**
 * Implements an inspection to detect when String references are compared using 'a==b' or 'a!=b'.
 * The quick fix converts these comparisons to 'a.equals(b) or '!a.equals(b)' respectively.
 */
class ComparingStringReferencesInspection : AbstractBaseJavaLocalInspectionTool() {
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
		return object : JavaElementVisitor() {
			/**
			 * 检测操作符是否包含 '==' 和 '!=',同时检测操作数是否是 String 类型。 检测忽略和null比较的情况。
			 * 如果满足条件，在ProblemsHolder中注册问题。(提示用户此处有问题)
			 */
			override fun visitBinaryExpression(expression: PsiBinaryExpression) {
				super.visitBinaryExpression(expression)
				val opSign = expression.operationTokenType
				if (opSign === JavaTokenType.EQEQ || opSign === JavaTokenType.NE) {
					// 是否满足检测条件, 操作数只有是null, 就返回
					val lOperand = expression.lOperand
					val rOperand = expression.rOperand
					if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) {
						return
					}
					// 检查操作数的类型
					if (isStringType(lOperand) || isStringType(rOperand)) {
						// 识别出了潜在的问题，注册"quick fix object"
						holder.registerProblem(expression, InspectionBundle.ourInstance.message("inspection.comparing.string.references.problem.descriptor") ?: "", myQuickFix)
					}
				}
			}

			private fun isStringType(operand: PsiExpression): Boolean {
				val type = operand.type as? PsiClassType ?: return false
				val resolvedType = type.resolve() ?: return false
				return "java.lang.String" == resolvedType.qualifiedName
			}

			private fun isNullLiteral(expression: PsiExpression): Boolean {
				return expression is PsiLiteralExpression &&
						expression.value == null
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
