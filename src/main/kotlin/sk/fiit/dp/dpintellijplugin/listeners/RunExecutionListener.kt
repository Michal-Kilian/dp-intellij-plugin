package sk.fiit.dp.dpintellijplugin.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.OSProcessUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object ExecutionState {
    @Volatile var appRunning: Boolean = false
}

class RunExecutionListener(
    private val project: Project,
) : ExecutionListener {
    private var jmxProfiler: JMXProfilerListener? = null
    private var jfrAgentListener: JFRAgentListener? = null

    override fun processStarted(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler
    ) {
        ExecutionState.appRunning = true

        val pid = tryGetPid(handler)
        println("PID = $pid")

        if (pid != null) {
            val scope = CoroutineScope(Dispatchers.Default)

            jmxProfiler = JMXProfilerListener(scope)
            jmxProfiler?.startProfiling(pid)

            jfrAgentListener = JFRAgentListener(project, scope)
            jfrAgentListener?.start(pid)
        }

        handler.addProcessListener(object : ProcessListener {
            override fun startNotified(event: ProcessEvent) {
                println("Start notified")
            }

            override fun processTerminated(event: ProcessEvent) {
                println("Process terminated. Exit code: ${event.exitCode}")
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                println("Output[$outputType]: ${event.text.trim()}")
            }
        })
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        ExecutionState.appRunning = false

        println("Process fully terminated: ${env.runProfile.name} (exit=$exitCode)")

        jmxProfiler?.stopProfiling()
        jmxProfiler = null

        jfrAgentListener?.stop()
        jfrAgentListener = null
    }

    private fun tryGetPid(handler: ProcessHandler): Long? {
        val proc = (handler as? OSProcessHandler)?.process ?: return null
        return try {
            OSProcessUtil.getProcessID(proc).toLong()
        } catch (e: Exception) {
            null
        }
    }
}