package sk.fiit.dp.dpintellijplugin.listeners

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType
import sk.fiit.dp.dpintellijplugin.services.OpenTabService

class OpenTabsUpdateListener(
    private val project: Project,
) : FileEditorManagerListener {

    override fun selectionChanged(event: FileEditorManagerEvent) {
        sendOpenTabs()
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        sendOpenTabs()
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        sendOpenTabs()
    }

    private fun sendOpenTabs() {
        val tabs = OpenTabService(project).getOpenTabs()
        WebSocketServerService.getInstance().sendMessage(MessageType.OPEN_TABS, tabs)
    }
}