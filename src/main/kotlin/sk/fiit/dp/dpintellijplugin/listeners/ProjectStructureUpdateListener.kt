package sk.fiit.dp.dpintellijplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.project.BaseMessage
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType

class ProjectStructureUpdateListener(
    private val project: Project,
) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        if (events.isEmpty()) return
        notifyAboutOutdatedProject()
    }

    private fun notifyAboutOutdatedProject() {
        val message = BaseMessage(
            message = "Project updated, city view is not up-to-date."
        )

        println("[Plugin] Sending projectOutdated notification to Unity.")
        WebSocketServerService.getInstance().sendMessage(MessageType.PROJECT_OUTDATED, message)
    }
}