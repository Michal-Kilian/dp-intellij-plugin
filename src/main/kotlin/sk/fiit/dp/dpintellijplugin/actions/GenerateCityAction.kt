package sk.fiit.dp.dpintellijplugin.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService

class GenerateCityAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        println("[Plugin] Generate City clicked.")
        WebSocketServerService.getInstance().sendProjectStructure()

        NotificationGroupManager.getInstance()
            .getNotificationGroup("dp-intellij-plugin")
            .createNotification("Project structure sent to Unity", NotificationType.INFORMATION)
            .notify(e.project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}