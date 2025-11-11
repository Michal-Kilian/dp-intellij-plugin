package sk.fiit.dp.dpintellijplugin.data.project

import com.google.gson.annotations.SerializedName

enum class CommandType {
    @SerializedName("pause")
    PAUSE,

    @SerializedName("resume")
    RESUME,
}

data class CommandMessage(
    val command: CommandType,
    val reason: String? = null,
)