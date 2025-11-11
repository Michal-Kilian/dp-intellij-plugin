package sk.fiit.dp.dpintellijplugin.listeners

import com.sun.tools.attach.VirtualMachine
import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import com.sun.management.OperatingSystemMXBean

class JMXProfilerListener(
    private val scope: CoroutineScope,
) {
    private var job: Job? = null
    private var connection: JMXConnector? = null
    private var mBeanServer: MBeanServerConnection? = null

    private var osBean: OperatingSystemMXBean? = null
    private var memBean: MemoryMXBean? = null

    fun startProfiling(pid: Long) {
        job = scope.launch(Dispatchers.IO) {
            try {
                val vm = VirtualMachine.attach(pid.toString())
                val address = vm.startLocalManagementAgent()
                vm.detach()

                connection = JMXConnectorFactory.connect(JMXServiceURL(address))
                mBeanServer = connection!!.mBeanServerConnection

                osBean = ManagementFactory.newPlatformMXBeanProxy(
                    mBeanServer,
                    "java.lang:type=OperatingSystem",
                    OperatingSystemMXBean::class.java,
                )
                memBean = ManagementFactory.newPlatformMXBeanProxy(
                    mBeanServer,
                    "java.lang:type=Memory",
                    MemoryMXBean::class.java,
                )

                println("JMX profiling started for pid=$pid")

                while (isActive) {
                    delay(1000)
                    pollMetrics(pid)
                }
            } catch (e: Exception) {
                println("JFR profiling failed: ${e.message}")
            } finally {
                cleanup()
            }
        }
    }

    private fun pollMetrics(pid: Long) {
        try {
            val os = osBean ?: return
            val mem = memBean ?: return

            val processCpuLoad = (os.processCpuLoad * 100).takeIf { it >= 0 } ?: 0.0
            val systemCpuLoad = (os.cpuLoad * 100).takeIf { it >= 0 } ?: 0.0
            val heapUsed = mem.heapMemoryUsage.used / (1024.0 * 1024.0)
            val heapMax = mem.heapMemoryUsage.max / (1024.0 * 1024.0)

            println("PID=$pid ProcessCPU=${"%.2f".format(processCpuLoad)}% SystemCPU=${"%.2f".format(systemCpuLoad)}% Heap=$heapUsed/$heapMax MB")
        } catch (e: Exception) {
            println("Failed to poll metrics: ${e.message}")
        }
    }

    fun stopProfiling() {
        job?.cancel()
        job = null
        cleanup()
        println("JMX profiling stopped")
    }

    private fun cleanup() {
        try {
            connection?.close()
        } catch (_: Exception) {}
        connection = null
        mBeanServer = null
        osBean = null
        memBean = null
    }
}