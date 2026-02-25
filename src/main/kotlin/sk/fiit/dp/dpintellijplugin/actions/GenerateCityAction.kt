package sk.fiit.dp.dpintellijplugin.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.services.NotificationService

class GenerateCityAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        println("[Plugin] Generate City clicked.")
        WebSocketServerService.getInstance().sendProjectStructure()
        NotificationService.getInstance().show("Project structure sent to Unity")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}