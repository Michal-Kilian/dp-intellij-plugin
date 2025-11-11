package sk.fiit.dp.dpintellijplugin.data.project

data class ExecutionSample(
    val timestamp: Long,
    val threadName: String,
    val frames: List<Frame>
)

data class Frame(
    val className: String,
    val method: String,
    val line: Int,
)