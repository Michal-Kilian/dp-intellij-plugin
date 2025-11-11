package sk.fiit.dp.dpintellijplugin.services

import com.intellij.openapi.project.Project;

object ProjectReferenceHolder {
    @Volatile
    var currentProject: Project? = null
}