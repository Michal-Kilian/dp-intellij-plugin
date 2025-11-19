package sk.fiit.dp.dpintellijplugin.data.collectors

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import sk.fiit.dp.dpintellijplugin.data.project.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProjectStructureCollector(private val project: Project) {

    fun collect(): ProjectStructure = ApplicationManager.getApplication()
        .runReadAction<ProjectStructure> {
            val flatPackages = collectFlat()
            val structure = ProjectStructure(projectName = project.name, packages = buildHierarchy(flatPackages))
            dumpStructureToFile(structure)

            structure
        }

    private fun collectFlat(): List<PackageNode> {
        val psiManager = PsiManager.getInstance(project)
        val files = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project))
        val javaFiles = files
            .mapNotNull { psiManager.findFile(it) as? PsiJavaFile }
            .filter { it.name != "module-info.java" }
            .filterNot { it.packageName.split('.').any { part -> part.equals("out", ignoreCase = true) } }

        println(javaFiles.size)

        val fileNodes = javaFiles.map { parseJavaFile(it) }
        return fileNodes
            .groupBy { it.packageName.ifBlank { "<default>" } }
            .map { (pkg, f) -> PackageNode(pkg, f) }
    }

    private fun collectJavaFiles(directory: PsiDirectory, out: MutableList<FileNode>) {
        directory.files.filterIsInstance<PsiJavaFile>().forEach { out += parseJavaFile(it) }
        directory.subdirectories.forEach { collectJavaFiles(it, out) }
    }

    private fun parseJavaFile(file: PsiJavaFile): FileNode {
        val importCount = file.importList?.allImportStatements?.size ?: 0
        val lineCount = file.text.lines().size
        val classes = file.classes.map { parseClass(it, file.virtualFile.path) }
        return FileNode(
            name = file.name,
            path = file.virtualFile.path,
            packageName = file.packageName,
            importCount = importCount,
            lineCount = lineCount,
            classes = classes
        )
    }

    private fun parseClass(cls: PsiClass, path: String): ClassNode {
        val methods = cls.methods.map { parseMethod(it, path) }
        val fields = cls.fields.map { parseField(it) }
        val innerClasses = cls.innerClasses.map { parseClass(it, path) }
        val modifiers = cls.modifierList?.text?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
        val (start, end) = getLineRange(cls)

        return ClassNode(
            name = cls.name ?: "<anon>",
            isInterface = cls.isInterface,
            isAbstract = cls.hasModifierProperty(PsiModifier.ABSTRACT),
            modifiers = modifiers,
            superClass = cls.superClass?.qualifiedName,
            interfaces = cls.interfaces.mapNotNull { it.qualifiedName },
            lineCount = end - start + 1,
            methodCount = methods.size,
            fieldCount = fields.size,
            methods = methods,
            fields = fields,
            innerClasses = innerClasses
        )
    }

    private fun parseMethod(m: PsiMethod, path: String): MethodNode {
        val (start, end) = getLineRange(m)
        return MethodNode(
            name = m.name,
            returnType = m.returnType?.presentableText,
            parameters = m.parameterList.parameters.map { it.type.presentableText },
            annotations = m.annotations.mapNotNull { it.qualifiedName },
            modifiers = m.modifierList.text.split("\\s+".toRegex()).filter { it.isNotBlank() },
            lineCount = end - start + 1,
            path = path,
            lineStart = start,
            lineEnd = end,
        )
    }

    private fun parseField(f: PsiField): FieldNode {
        return FieldNode(
            name = f.name,
            type = f.type.presentableText,
            annotations = f.annotations.mapNotNull { it.qualifiedName },
            modifiers = f.modifierList?.text?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
        )
    }

    private fun getLineRange(e: PsiElement): Pair<Int, Int> {
        val vf = e.containingFile.virtualFile ?: return 0 to 0
        val doc: Document = FileDocumentManager.getInstance().getDocument(vf) ?: return 0 to 0
        return try {
            val start = doc.getLineNumber(e.textRange.startOffset) + 1
            val end = doc.getLineNumber(e.textRange.endOffset) + 1
            start to end
        } catch (_: Exception) {
            0 to 0
        }
    }

    private fun buildHierarchy(flat: List<PackageNode>): List<PackageNode> {
        if (flat.isEmpty()) return emptyList()

        val packageMap = mutableMapOf<String, PackageNode>()
        flat.forEach { pkg ->
            packageMap[pkg.name] = PackageNode(pkg.name, pkg.files.toMutableList(), mutableListOf())
        }

        val entries = packageMap.toList()

        for ((name, pkg) in entries) {
            val parentName = name.substringBeforeLast('.', missingDelimiterValue = "")
            if (parentName.isNotEmpty()) {
                val parent = packageMap.getOrPut(parentName) {
                    PackageNode(parentName, emptyList(), mutableListOf())
                }
                parent.subPackages.add(pkg)
            }
        }

        val allChildren = packageMap.values.flatMap { it.subPackages }
        return packageMap.values.filterNot { it in allChildren }
    }

    private fun dumpStructureToFile(structure: ProjectStructure) {
        try {
            val baseDir = project.basePath ?: return
            val dumpDir = File(baseDir, "structure-dump")
            if (!dumpDir.exists()) dumpDir.mkdirs()
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd__HHmmss"))
            val dumpFile = File(dumpDir, "structure_$timestamp.txt")
            val json = Gson().toJson(structure)
            dumpFile.writeText(json)
        } catch (_: Exception) { }
    }
}

object JsonUtil {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun pretty(any: Any): String = gson.toJson(any)
}