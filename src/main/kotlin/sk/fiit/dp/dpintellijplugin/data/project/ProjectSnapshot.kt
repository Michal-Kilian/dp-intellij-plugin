package sk.fiit.dp.dpintellijplugin.data.project

data class ProjectSnapshot(
    val projectName: String,
    val modules: List<ModuleInfo>,
    val sdkName: String,
)

data class ModuleInfo(
    val moduleName: String,
    val sdkName: String,
)