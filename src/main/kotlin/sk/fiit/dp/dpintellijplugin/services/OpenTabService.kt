package sk.fiit.dp.dpintellijplugin.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.html.dom.document
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.project.OpenTabs

@Service(Service.Level.PROJECT)
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

    fun openTab(path: String, line: Int? = null) {
        val vf: VirtualFile? = VirtualFileManager.getInstance().findFileByUrl("file://$path")

        if (vf != null) {
            ApplicationManager.getApplication().invokeLater {
                val editorManager = FileEditorManager.getInstance(project)
                val editors = editorManager.openFile(vf, true)

                if (line != null && line > 0) {
                    ApplicationManager.getApplication().invokeLater {
                        editors.filterIsInstance<TextEditor>()
                            .firstOrNull()
                            ?.editor
                            ?.let { editor ->
                                val caretModel = editor.caretModel
                                val document = editor.document

                                val targetLine = line.coerceAtMost(document.lineCount)
                                val offset = document.getLineStartOffset(targetLine - 1)

                                caretModel.moveToOffset(offset)
                                editor.scrollingModel.scrollToCaret(
                                    ScrollType.CENTER,
                                )
                            }
                    }
                }
            }
        } else {
            println("Could not open tab, file not found: $path")
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) : OpenTabService = project.service()
    }
}