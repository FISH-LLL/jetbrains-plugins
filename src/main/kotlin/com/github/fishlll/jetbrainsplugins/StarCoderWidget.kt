package com.github.fishlll.jetbrainsplugins

import com.github.fishlll.jetbrainsplugins.settings.StarCoderSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.Multiframe
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.event.MouseEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.Icon

class StarCoderWidget(project: Project) : EditorBasedWidget(project), Multiframe, StatusBarWidget.IconPresentation,
	CaretListener, SelectionListener, BulkAwareDocumentListener.Simple, PropertyChangeListener {
	private var serviceQueue: MergingUpdateQueue? = null
	override fun ID(): @NonNls String {
		return ID
	}

	override fun copy(): StatusBarWidget {
		return StarCoderWidget(project)
	}

	override fun getIcon(): Icon? {
		val starCoder = ApplicationManager.getApplication().getService(
			StarCoderService::class.java
		)
		val status: StarCoderStatus = StarCoderStatus.Companion.getStatusByCode(starCoder.status)
		return if (status == StarCoderStatus.OK) {
			if (StarCoderSettings.instance.isSaytEnabled) StarCoderIcons.Companion.WidgetEnabled else StarCoderIcons.Companion.WidgetDisabled
		} else {
			StarCoderIcons.Companion.WidgetError
		}
	}

	override fun getPresentation(): WidgetPresentation? {
		return this
	}

	override fun getTooltipText(): @NlsContexts.Tooltip String? {
		val toolTipText = StringBuilder("StarCoder")
		if (StarCoderSettings.instance.isSaytEnabled) {
			toolTipText.append(" enabled")
		} else {
			toolTipText.append(" disabled")
		}
		val starCoder = ApplicationManager.getApplication().getService(
			StarCoderService::class.java
		)
		val statusCode = starCoder.status
		val status: StarCoderStatus = StarCoderStatus.Companion.getStatusByCode(statusCode)
		when (status) {
			StarCoderStatus.OK -> if (StarCoderSettings.instance.isSaytEnabled) {
				toolTipText.append(" (Click to disable)")
			} else {
				toolTipText.append(" (Click to enable)")
			}

			StarCoderStatus.UNKNOWN -> {
				toolTipText.append(" (http error ")
				toolTipText.append(statusCode)
				toolTipText.append(")")
			}

			else -> {
				toolTipText.append(" (")
				toolTipText.append(status.displayValue)
				toolTipText.append(")")
			}
		}
		return toolTipText.toString()
	}

	override fun getClickConsumer(): com.intellij.util.Consumer<MouseEvent>? {
		// Toggle if the plugin is enabled.
		return com.intellij.util.Consumer { mouseEvent: MouseEvent? ->
			StarCoderSettings.instance.toggleSaytEnabled()
			if (myStatusBar != null) {
				myStatusBar.updateWidget(ID)
			}
		}
	}

	override fun install(statusBar: StatusBar) {
		super.install(statusBar)
		serviceQueue = MergingUpdateQueue(
			"StarCoderServiceQueue", 1000, true,
			null, this, null, false
		)
		serviceQueue!!.setRestartTimerOnAdd(true)
		val multicaster = EditorFactory.getInstance().eventMulticaster
		multicaster.addCaretListener(this, this)
		multicaster.addSelectionListener(this, this)
		multicaster.addDocumentListener(this, this)
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addPropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY, this)
		Disposer.register(
			this
		) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
				SWING_FOCUS_OWNER_PROPERTY,
				this
			)
		}
	}

	private val focusOwnerEditor: Editor?
		private get() {
			val component = focusOwnerComponent
			val editor = if (component is EditorComponentImpl) component.editor else editor
			return if (editor != null && !editor.isDisposed) editor else null
		}
	private val focusOwnerComponent: Component?
		private get() {
			var focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
			if (focusOwner == null) {
				val focusManager = IdeFocusManager.getInstance(project)
				val frame = focusManager.lastFocusedIdeWindow
				if (frame != null) {
					focusOwner = focusManager.getLastFocusedFor(frame)
				}
			}
			return focusOwner
		}

	private fun isFocusedEditor(editor: Editor): Boolean {
		val focusOwner = focusOwnerComponent
		return focusOwner === editor.contentComponent
	}

	override fun propertyChange(evt: PropertyChangeEvent) {
		updateInlayHints(focusOwnerEditor)
	}

	override fun selectionChanged(event: SelectionEvent) {
		updateInlayHints(event.editor)
	}

	override fun caretPositionChanged(event: CaretEvent) {
		updateInlayHints(event.editor)
	}

	override fun caretAdded(event: CaretEvent) {
		updateInlayHints(event.editor)
	}

	override fun caretRemoved(event: CaretEvent) {
		updateInlayHints(event.editor)
	}

	override fun afterDocumentChange(document: Document) {
		if (ApplicationManager.getApplication().isDispatchThread) {
			EditorFactory.getInstance().editors(document)
				.filter { editor: Editor -> isFocusedEditor(editor) }
				.findFirst()
				.ifPresent { focusedEditor: Editor? -> updateInlayHints(focusedEditor) }
		}
	}

	private fun updateInlayHints(focusedEditor: Editor?) {
		if (focusedEditor == null) return
		// TODO File extension exclusion settings?
		val file = FileDocumentManager.getInstance().getFile(focusedEditor.document) ?: return

		// If a selection is highlighted, clear all hints.
		val selection = focusedEditor.caretModel.currentCaret.selectedText
		if (selection != null && selection.length > 0) {
			val existingHints = file.getUserData(STAR_CODER_CODE_SUGGESTION)
			if (existingHints != null && existingHints.size > 0) {
				file.putUserData(STAR_CODER_CODE_SUGGESTION, null)
				file.putUserData(STAR_CODER_POSITION, focusedEditor.caretModel.offset)
				val inlayModel = focusedEditor.inlayModel
				inlayModel.getInlineElementsInRange(
					0,
					focusedEditor.document.textLength,
					CodeGenHintRenderer::class.java
				).forEach(
					java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
				inlayModel.getBlockElementsInRange(
					0,
					focusedEditor.document.textLength,
					CodeGenHintRenderer::class.java
				).forEach(
					java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
			}
			return
		}
		val starCoderPos = file.getUserData(STAR_CODER_POSITION)
		val lastPosition = starCoderPos ?: 0
		val currentPosition = focusedEditor.caretModel.offset

		// If cursor hasn't moved, don't do anything.
		if (lastPosition == currentPosition) return

		// Check the existing inline hint (not blocks) if it exists.
		val inlayModel = focusedEditor.inlayModel
		if (currentPosition > lastPosition) {
			var existingHints = file.getUserData<Array<String>>(STAR_CODER_CODE_SUGGESTION)
			if (existingHints != null && existingHints.size > 0) {
				var inlineHint = existingHints[0]
				var modifiedText =
					focusedEditor.document.charsSequence.subSequence(lastPosition, currentPosition).toString()
				if (modifiedText.startsWith("\n")) {
					// If the user typed Enter, the editor may have auto-spaced for alignment.
					modifiedText = modifiedText.replace(" ", "")
					// TODO Count the spaces and remove from the next block hint, or just remove
					// leading spaces from the block hint before moving up?
					// example: set a boolean here and do existingHints[1] = existingHints[1].stripLeading()
					// The problem is that the spaces are split in the update, some spaces are included after the carriage return,
					// (in the caret position update) but then after document change has more spaces in it.
				}
				// See if they typed the same thing that we suggested.
				if (inlineHint!!.startsWith(modifiedText)) {
					// Update the hint rather than calling the API to suggest a new one.
					inlineHint = inlineHint.substring(modifiedText.length)
					if (inlineHint.length > 0) {
						// We only need to modify the inline hint and any block hints will remain unchanged.
						inlayModel.getInlineElementsInRange(
							0,
							focusedEditor.document.textLength,
							CodeGenHintRenderer::class.java
						).forEach(
							java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
						inlayModel.addInlineElement(currentPosition, true, CodeGenHintRenderer(inlineHint))
						existingHints[0] = inlineHint

						// Update the UserData
						file.putUserData(STAR_CODER_CODE_SUGGESTION, existingHints)
						file.putUserData(STAR_CODER_POSITION, currentPosition)
						return
					} else if (existingHints.size > 1) {
						// If the first line has been completely inserted, and there are more lines, move them up.
						existingHints = Arrays.copyOfRange(existingHints, 1, existingHints.size)
						addCodeSuggestion(focusedEditor, file, currentPosition, existingHints)
						return
					} else {
						// We ran out of inline hint and there are no block hints,
						// So clear the hints now, and we'll call the API below.
						file.putUserData(STAR_CODER_CODE_SUGGESTION, null)
					}
				}
			}
		}

		// If we made it through all that, clear all hints and call the API.
		inlayModel.getInlineElementsInRange(0, focusedEditor.document.textLength, CodeGenHintRenderer::class.java)
			.forEach(
				java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
		inlayModel.getBlockElementsInRange(0, focusedEditor.document.textLength, CodeGenHintRenderer::class.java)
			.forEach(
				java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })

		// Update position immediately to prevent repeated calls.
		file.putUserData(STAR_CODER_POSITION, currentPosition)
		val starCoder = ApplicationManager.getApplication().getService(
			StarCoderService::class.java
		)
		val editorContents = focusedEditor.document.charsSequence
		println("Queued update: " + currentPosition + " for " + file.name)
		serviceQueue!!.queue(Update.create(focusedEditor) {
			val hintList = starCoder.getCodeCompletionHints(editorContents, currentPosition)
			addCodeSuggestion(focusedEditor, file, currentPosition, hintList)
		})
	}

	//    private void disposeInlayHints(Inlay<?> inlay) {
	//        if(inlay.getRenderer() instanceof CodeGenHintRenderer) {
	//            inlay.dispose();
	//        }
	//    }
	private fun addCodeSuggestion(
		focusedEditor: Editor,
		file: VirtualFile,
		suggestionPosition: Int,
		hintList: Array<String>?
	) {
		WriteCommandAction.runWriteCommandAction(focusedEditor.project) {

			// Discard this update if the position has changed or text is now selected.
			if (suggestionPosition != focusedEditor.caretModel.offset) return@runWriteCommandAction
			if (focusedEditor.selectionModel.selectedText != null) return@runWriteCommandAction
			file.putUserData(STAR_CODER_CODE_SUGGESTION, hintList)
			file.putUserData(STAR_CODER_POSITION, suggestionPosition)
			val inlayModel = focusedEditor.inlayModel
			inlayModel.getInlineElementsInRange(0, focusedEditor.document.textLength, CodeGenHintRenderer::class.java)
				.forEach(
					java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
			inlayModel.getBlockElementsInRange(0, focusedEditor.document.textLength, CodeGenHintRenderer::class.java)
				.forEach(
					java.util.function.Consumer { obj: Inlay<out CodeGenHintRenderer?> -> obj.dispose() })
			if (hintList != null && hintList.size > 0) {
				// The first line is an inline element
				if (hintList[0]!!.trim { it <= ' ' }.length > 0) {
					inlayModel.addInlineElement(suggestionPosition, true, CodeGenHintRenderer(hintList[0]))
				}
				// Each additional line is a block element
				for (i in 1 until hintList.size) {
					inlayModel.addBlockElement(
						suggestionPosition, false, false, 0, CodeGenHintRenderer(
							hintList[i]
						)
					)
				}
			}
			println("Completed update: " + suggestionPosition + " for " + file.name)
		}
	}

	companion object {
		const val ID = "StarCoderWidget"
		@JvmField
		val STAR_CODER_CODE_SUGGESTION = Key<Array<String>>("StarCoder Code Suggestion")
		@JvmField
		val STAR_CODER_POSITION = Key<Int>("StarCoder Position")
	}
}
