package sk.fiit.dp.dpintellijplugin.communication

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import sk.fiit.dp.dpintellijplugin.data.collectors.ProjectSnapshotCollector
import sk.fiit.dp.dpintellijplugin.data.collectors.ProjectStructureCollector
import sk.fiit.dp.dpintellijplugin.data.project.CommandMessage
import sk.fiit.dp.dpintellijplugin.data.project.CommandType
import sk.fiit.dp.dpintellijplugin.data.ws.MessageType
import sk.fiit.dp.dpintellijplugin.data.ws.WebSocketMessage
import sk.fiit.dp.dpintellijplugin.services.OpenTabService
import sk.fiit.dp.dpintellijplugin.services.ProjectReferenceHolder
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.util.Base64
import java.util.zip.GZIPOutputStream

@Service(Service.Level.APP)
class WebSocketServerService() : WebSocketServer(InetSocketAddress(8765)) {

    private val gson = Gson();
    private val connections = mutableSetOf<WebSocket>()
    @Volatile private var started = false
    @Volatile private var executionSamplePaused = false

    fun executionSamplePaused() = executionSamplePaused

    fun setExecutionSamplePaused(value: Boolean) {
        executionSamplePaused = value
    }

    override fun onStart() {
        println("IntelliJ WebSocket server listening on ws://localhost:8765")
    }

    fun ensureStarted() {
        if (!started) {
            synchronized(this) {
                if (!started) {
                    super.start()
                    started = true
                }
            }
        } else {
            println("WebSocket server already running")
        }
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        connections.add(conn)
        println("Unity connected: ${conn.remoteSocketAddress}")
        sendInitialProjectData()
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        connections.remove(conn)
        println("Unity disconnected: $reason")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        println("From Unity: $message")
        handleIncomingMessage(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        println("Server error: ${ex.message}")
    }

    fun <T> sendMessage(type: MessageType, payload: T) {
        val msg = WebSocketMessage<T>(
            type = type,
            source = "IntelliJ",
            data = payload,
        )
        val json = gson.toJson(msg)
        // println("[Server] Outgoing JSON length: ${json.toByteArray(Charsets.UTF_8).size} bytes")
        // println("Broadcast -> $json")
        broadcast(json)
    }

    private fun handleIncomingMessage(raw: String) {
        try {
            val envelope = gson.fromJson(raw, WebSocketMessage::class.java)
            when (envelope.type) {
                MessageType.REQUEST_PROJECT_STRUCTURE -> {
                    println("Unity requested updated project structure.")
                    sendProjectStructure()
                }
                MessageType.COMMAND -> {
                    val messageType = object : TypeToken<WebSocketMessage<CommandMessage>>() {}.type
                    val fullMessage: WebSocketMessage<CommandMessage> = gson.fromJson(raw, messageType)
                    val commandMessage = fullMessage.data
                    when (commandMessage.command){
                        CommandType.OPEN_IN_IDE -> {
                            println("Unity requested opening a method in IDE: ${commandMessage.path}")
                            val project = ProjectReferenceHolder.currentProject
                            if (project == null) {
                                println("No project loaded. Cannot open file.")
                                return
                            }
                            val path = commandMessage.path ?: run {
                                println("No path provided.")
                                return
                            }
                            val line = commandMessage.line ?: 0
                            val openTabService = OpenTabService.getInstance(project)
                            openTabService.openTab(
                                path = path,
                                line = line
                            )
                        }
                        else -> { }
                    }
                }
                else -> println("Unknown message type from Unity: ${envelope.type}")
            }
        } catch (ex: Exception) {
            println("Parse error: ${ex.message}")
        }
    }

    private fun sendInitialProjectData() {
        val project = ProjectReferenceHolder.currentProject
        if (project == null) {
            println("No project loaded. Cannot send initial data.")
            return
        }

        try {
            val snapshot = ProjectSnapshotCollector(project).collect()
            sendMessage(MessageType.PROJECT_SNAPSHOT, snapshot)

            val openTabs = OpenTabService(project).getOpenTabs()
            sendMessage(MessageType.OPEN_TABS, openTabs)

            // val structure = ProjectStructureCollector(project).collect()
            // sendMessage(MessageType.PROJECT_STRUCTURE, structure)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun sendProjectStructure() {
        val project = ProjectReferenceHolder.currentProject
        if (project == null) {
            println("No project loaded. Cannot send project structure.")
            return
        }

        try {
            val structure = ProjectStructureCollector(project).collect()
            sendMessage(MessageType.PROJECT_STRUCTURE, structure)
            println("Sent project structure to Unity:\n${structure}")
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun sendCommand(type: CommandType, reason: String? = null) {
        val message = CommandMessage(
            command = type,
            reason = reason,
            path = null,
            line = null,
        )
        sendMessage(MessageType.COMMAND, message)
        println("Sent command: ${type.name} (${reason ?: "-"})")
    }

    private fun compressToBase64(input: String): String {
        val byteOut = ByteArrayOutputStream()
        GZIPOutputStream(byteOut).bufferedWriter(Charsets.UTF_8).use { it.write(input) }
        return Base64.getEncoder().encodeToString(byteOut.toByteArray())
    }

    companion object {
        fun getInstance() : WebSocketServerService = service()
    }
}