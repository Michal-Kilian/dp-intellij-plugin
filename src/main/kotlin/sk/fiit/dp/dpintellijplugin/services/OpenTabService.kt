package sk.fiit.dp.dpintellijplugin.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import sk.fiit.dp.dpintellijplugin.data.project.OpenTabs

class OpenTabService(
    private val project: Project,
) {
    fun getOpenTabs(): OpenTabs {
        val manager = FileEditorManager.getInstance(project)
        val openTabs = manager.openFiles.map { it.path }
        return OpenTabs(
            count = openTabs.size,
            tabs = openTabs,
        )
    }

    fun openTab(path: String) {
        val vf: VirtualFile? = VirtualFileManager.getInstance().findFileByUrl("file://$path")

        if (vf != null) {
            ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openFile(vf, true)
            }
        } else {
            println("Could not open tab, file not found: $path")
        }
    }
}