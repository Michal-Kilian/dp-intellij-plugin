package sk.fiit.dp.dpintellijplugin.data.collectors

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import sk.fiit.dp.dpintellijplugin.data.project.ModuleInfo
import sk.fiit.dp.dpintellijplugin.data.project.ProjectSnapshot

class ProjectSnapshotCollector(
    private val project: Project,
) {
    fun collect(): ProjectSnapshot {
        val modules = ModuleManager.getInstance(project)
            .modules
            .map { m ->
                val sdk = ModuleRootManager.getInstance(m).sdk?.name ?: "No SDK"
                ModuleInfo(m.name, sdk)
            }

        val projectSdk = ProjectRootManager.getInstance(project).projectSdk?.name
            ?: "No project-level SDK"

        return ProjectSnapshot(
            projectName = project.name,
            modules = modules,
            sdkName = projectSdk
        )
    }
}