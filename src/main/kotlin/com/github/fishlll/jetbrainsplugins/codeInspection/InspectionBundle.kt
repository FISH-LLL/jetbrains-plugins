// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.fishlll.jetbrainsplugins.codeInspection

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.InspectionBundle"

class InspectionBundle : DynamicBundle(BUNDLE) {

	fun message(key: @PropertyKey(resourceBundle = BUNDLE) String,  vararg params: Any): @Nls String? {
		return ourInstance.getMessage(key, *params)
	}

	companion object {
		val ourInstance: InspectionBundle = InspectionBundle()
	}
}


