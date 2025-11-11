package sk.fiit.dp.dpintellijplugin.services
import com.intellij.ProjectTopics
import com.intellij.execution.ExecutionManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFileManager
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.listeners.OpenTabsUpdateListener
import sk.fiit.dp.dpintellijplugin.listeners.ProjectSnapshotUpdateListener
import sk.fiit.dp.dpintellijplugin.listeners.ProjectStructureUpdateListener
import sk.fiit.dp.dpintellijplugin.listeners.RunExecutionListener

class ProjectStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        ProjectReferenceHolder.currentProject = project

        val server = WebSocketServerService.getInstance()
        server.ensureStarted()

        subscribeToUpdates(project)
    }

    private fun subscribeToUpdates(project: Project) {
        val scope = project.service<ProjectCoroutineScopeService>()

        val connection = project.messageBus.connect(scope as Disposable)

        connection.subscribe(
            ProjectTopics.PROJECT_ROOTS,
            ProjectSnapshotUpdateListener(project)
        )

        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            ProjectStructureUpdateListener(project)
        )

        connection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            OpenTabsUpdateListener(project)
        )

        connection.subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            RunExecutionListener(project)
        )
    }
}
