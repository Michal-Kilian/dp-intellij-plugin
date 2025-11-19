package sk.fiit.dp.dpintellijplugin.data.ws

import com.google.gson.annotations.SerializedName

enum class MessageType {
    @SerializedName("projectStructure")
    PROJECT_STRUCTURE,

    @SerializedName("executionSample")
    EXECUTION_SAMPLE,

    @SerializedName("projectSnapshot")
    PROJECT_SNAPSHOT,

    @SerializedName("openTabs")
    OPEN_TABS,

    @SerializedName("projectOutdated")
    PROJECT_OUTDATED,

    @SerializedName("command")
    COMMAND,

    @SerializedName("requestProjectStructure")
    REQUEST_PROJECT_STRUCTURE,
}

data class WebSocketMessage<T>(
    val type: MessageType,
    val source: String,
    val timestamp: Long = System.currentTimeMillis(),
    val data: T,
    //val compressed: Boolean = false,
    //val compressedData: String? = null,
)