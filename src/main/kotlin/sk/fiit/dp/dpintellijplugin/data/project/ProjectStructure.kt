package sk.fiit.dp.dpintellijplugin.data.project

import java.util.UUID

data class ProjectStructure(
    val projectName: String,
    var packages: List<PackageNode>,
)

data class PackageNode(
    val name: String,
    val files: List<FileNode>,
    val subPackages: MutableList<PackageNode> = mutableListOf()
)

data class FileNode(
    val name: String,
    val path: String,
    val packageName: String,
    val importCount: Int,
    val lineCount: Int,
    val classes: List<ClassNode>
)

data class ClassNode(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isInterface: Boolean,
    val isAbstract: Boolean,
    val modifiers: List<String>,
    val superClass: String?,
    val interfaces: List<String>,
    val lineCount: Int,
    val methodCount: Int,
    val fieldCount: Int,
    val methods: List<MethodNode>,
    val fields: List<FieldNode>,
    val innerClasses: List<ClassNode>
)

data class MethodNode(
    val name: String,
    val returnType: String?,
    val parameters: List<String>,
    val annotations: List<String>,
    val modifiers: List<String>,
    val lineCount: Int,
    val path: String,
    val lineStart: Int? = null,
    val lineEnd: Int? = null,
)

data class FieldNode(
    val name: String,
    val type: String?,
    val annotations: List<String>,
    val modifiers: List<String>,
)