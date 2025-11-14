package sk.fiit.dp.dpintellijplugin.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm

@Service(Service.Level.APP)
class NotificationService : Disposable {
    private val alarm = Alarm(this)

    fun show(
        message: String,
        type: NotificationType = NotificationType.INFORMATION,
        project: Project? = ProjectReferenceHolder.currentProject,
        title: String = "",
        timeoutMs: Int = 3000
    ) {
        val notification: Notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(
                title = title,
                content = message,
                type = type,
            )
        notification.notify(project)
        alarm.addRequest({
            notification.expire()
        }, timeoutMs)
    }

    override fun dispose() {
        alarm.dispose()
    }

    companion object {
        const val NOTIFICATION_GROUP = "dp-intellij-plugin"

        @JvmStatic
        fun getInstance(): NotificationService = service()
    }
}