package sk.fiit.dp.dpintellijplugin.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.icons.AllIcons.Actions
import com.intellij.openapi.actionSystem.ActionUpdateThread
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.project.CommandType
import sk.fiit.dp.dpintellijplugin.listeners.ExecutionState

class ToggleExecutionStreamAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val ws = WebSocketServerService.getInstance()

        ws.setExecutionSamplePaused(!ws.executionSamplePaused())

        if (ws.executionSamplePaused()) {
            ws.sendCommand(CommandType.PAUSE, "User paused live profiling")
            e.presentation.text = "Resume Execution Stream"
            e.presentation.icon = Actions.Resume
            NotificationGroupManager.getInstance()
                .getNotificationGroup("dp-intellij-plugin")
                .createNotification("Pausing execution samples", NotificationType.INFORMATION)
                .notify(e.project)
        } else {
            ws.sendCommand(CommandType.RESUME)
            e.presentation.text = "Pause Execution Stream"
            e.presentation.icon = Actions.Pause
            NotificationGroupManager.getInstance()
                .getNotificationGroup("dp-intellij-plugin")
                .createNotification("Resuming execution samples", NotificationType.INFORMATION)
                .notify(e.project)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = ExecutionState.appRunning
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}