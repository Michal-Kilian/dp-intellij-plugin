package sk.fiit.dp.dpintellijplugin.listeners

import com.google.gson.Gson
import com.intellij.openapi.project.Project
import com.sun.tools.attach.VirtualMachine
import kotlinx.coroutines.*
import sk.fiit.dp.dpintellijplugin.communication.WebSocketServerService
import sk.fiit.dp.dpintellijplugin.data.project.ExecutionSample
import sk.fiit.dp.dpintellijplugin.data.project.ProjectStructure
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType
import java.io.BufferedReader
import java.io.File
import java.net.ServerSocket
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class JFRAgentListener(
    private val project: Project,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    private var gson: Gson = Gson()

    fun start(pid: Long, port: Int = 5555) {
        job = scope.launch(Dispatchers.IO) {
            val server = ServerSocket(port)
            println("[Plugin] Listening for JFR agent connection on port $port")

            val vm = VirtualMachine.attach(pid.toString())
            val agentJar = Path.of("C:\\Users\\kilia\\dp-intellij-plugin\\jfr-agent\\build\\libs\\jfr-agent-1.0-SNAPSHOT-all.jar")
            println("[Plugin] Injecting agent $agentJar")
            vm.loadAgent(agentJar.toString(), "port=$port")
            vm.detach()

            val socket = server.accept()
            println("[Plugin] Agent connected")
            val reader: BufferedReader = socket.getInputStream().bufferedReader()

            reader.forEachLine { line ->
                try {
                    val sample = gson.fromJson(line, ExecutionSample::class.java)
                    if (!WebSocketServerService.getInstance().executionSamplePaused())
                        WebSocketServerService.getInstance().sendMessage(MessageType.EXECUTION_SAMPLE, sample)
                    //dumpSampleToFile(sample)
                } catch (ex: java.net.SocketException) {
                    println("[Plugin] Agent socket closed: ${ex.message}")
                    return@forEachLine
                } catch (ex: Exception) {
                    println("[Plugin] Read error: ${ex.message}")
                }
            }

            println("[Plugin] Agent connection closed")
        }
    }

    private fun dumpSampleToFile(sample: ExecutionSample) {
        try {
            val baseDir = project.basePath ?: return
            val dumpDir = File(baseDir, "sample-dump")
            if (!dumpDir.exists()) dumpDir.mkdirs()
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd__HHmmss"))
            val dumpFile = File(dumpDir, "sample_$timestamp.txt")
            val json = Gson().toJson(sample)
            dumpFile.writeText(json)
        } catch (_: Exception) { }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}