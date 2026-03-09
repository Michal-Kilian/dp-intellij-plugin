package sk.fiit.dp.dpintellijplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Service(Service.Level.APP)
class InteractionCounterService : Disposable {
    private val startedAt: LocalDateTime = LocalDateTime.now()
    private val sessionId: String = buildSessionId()

    private val clickCount = AtomicLong(0)
    private val keyPressCount = AtomicLong(0)

    private val manualExportHappened = AtomicBoolean(false)

    private val listener = AWTEventListener { event ->
        when (event) {
            is MouseEvent -> {
                if (event.id == MouseEvent.MOUSE_CLICKED) {
                    clickCount.incrementAndGet()
                }
            }

            is KeyEvent -> {
                if (event.id == KeyEvent.KEY_PRESSED) {
                    keyPressCount.incrementAndGet()
                }
            }
        }
    }

    init {
        println("InteractionCounterService initialized")
        Toolkit.getDefaultToolkit().addAWTEventListener(
            listener,
            AWTEvent.MOUSE_EVENT_MASK or AWTEvent.KEY_EVENT_MASK
        )
    }

    fun getSessionId(): String = sessionId

    fun getClickCount(): Long = clickCount.get()

    fun getKeyPressCount(): Long = keyPressCount.get()

    fun exportNow(): File {
        val file = sessionFile()
        appendSnapshot(file)
        manualExportHappened.set(true)
        return file
    }

    private fun sessionFile(): File {
        val dir = defaultExportDir()
        Files.createDirectories(dir)
        return dir.resolve("idea-interaction-counts-$sessionId.csv").toFile()
    }

    private fun defaultExportDir(): Path {
        return Path.of(PathManager.getLogPath(), "input-counter")
    }

    private fun appendSnapshot(file: File) {
        val fileAlreadyExists = file.exists()
        val exportedAt = LocalDateTime.now()

        file.parentFile?.mkdirs()

        val text = buildString {
            if (!fileAlreadyExists) {
                appendLine(
                    listOf(
                        "session_id",
                        "started_at",
                        "exported_at",
                        "click_count",
                        "key_press_count"
                    ).joinToString(",")
                )
            }

            appendLine(
                listOf(
                    csv(sessionId),
                    csv(startedAt.toString()),
                    csv(exportedAt.toString()),
                    clickCount.get().toString(),
                    keyPressCount.get().toString()
                ).joinToString(",")
            )
        }

        file.appendText(text, StandardCharsets.UTF_8)
    }

    private fun csv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    override fun dispose() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(listener)

        if (!manualExportHappened.get()) {
            appendSnapshot(sessionFile())
        }
    }

    companion object {
        private val SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd-HHmmss"
        )

        fun getInstance(): InteractionCounterService = service()

        private fun buildSessionId(): String {
            val ts = LocalDateTime.now().format(SESSION_TIME_FORMATTER)
            val shortId = UUID.randomUUID().toString().substring(0, 8)
            return "$ts-$shortId"
        }
    }
}