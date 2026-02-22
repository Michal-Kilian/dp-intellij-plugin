package sk.fiit.dp.dpintellijplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.project.BaseMessage
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType

class ProjectStructureUpdateListener(
    private val project: Project,
) : BulkFileListener {
    private var lastNotificationTime = 0L;
    private val debounceMs = 1000;

    override fun after(events: List<VFileEvent>) {
        if (events.isEmpty()) return

        val relevantChange = events.any { event -> event is VFileContentChangeEvent }
        if (relevantChange) {
            notifyAboutOutdatedProject()
        }
    }

    private fun notifyAboutOutdatedProject() {
        val now = System.currentTimeMillis()
        if (now - lastNotificationTime < debounceMs) return
        lastNotificationTime = now

        val message = BaseMessage(
            message = "Project updated, city view is not up-to-date."
        )

        println("[Plugin] Sending projectOutdated notification to Unity.")
        WebSocketServerService.getInstance().sendMessage(MessageType.PROJECT_OUTDATED, message)
    }
}