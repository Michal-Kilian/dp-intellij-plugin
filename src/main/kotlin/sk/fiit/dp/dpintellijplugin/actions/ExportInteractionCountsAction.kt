package sk.fiit.dp.dpintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import sk.fiit.dp.dpintellijplugin.services.InteractionCounterService

class ExportInteractionCountsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val service = InteractionCounterService.getInstance()
        val file = service.exportNow()

        Messages.showInfoMessage(
            e.project,
            buildString {
                appendLine("Session exported successfully.")
                appendLine()
                appendLine("Session ID: ${service.getSessionId()}}")
                appendLine("Clicks: ${service.getSessionId()}")
                appendLine("Key presses: ${service.getClickCount()}")
                appendLine()
                appendLine("File:")
                append(file.absolutePath)
            },
            "Interaction Counter"
        )
    }
}