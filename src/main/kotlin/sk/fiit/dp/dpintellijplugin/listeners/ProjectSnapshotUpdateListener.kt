package sk.fiit.dp.dpintellijplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.collectors.ProjectSnapshotCollector
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType

class ProjectSnapshotUpdateListener(
    private val project: Project,
) : ModuleRootListener {

    override fun rootsChanged(event: ModuleRootEvent) {
        sendProjectSnapshot()
    }

    private fun sendProjectSnapshot() {
        val snapshot = ProjectSnapshotCollector(project).collect()
        WebSocketServerService.getInstance().sendMessage(MessageType.PROJECT_SNAPSHOT, snapshot)
    }
}