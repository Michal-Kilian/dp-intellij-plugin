package sk.fiit.dp.dpintellijplugin.data.project

import com.google.gson.annotations.SerializedName

enum class CommandType {
    @SerializedName("pause")
    PAUSE,

    @SerializedName("resume")
    RESUME,

    @SerializedName("openInIDE")
    OPEN_IN_IDE,
}

data class CommandMessage(
    val command: CommandType,
    val reason: String? = null,
    val path: String? = null,
    val line: Int? = null,
)