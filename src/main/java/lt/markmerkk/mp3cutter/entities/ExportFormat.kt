package lt.markmerkk.mp3cutter.entities

enum class ExportFormat {
    INVALID,
    MP3,
    MP4,
    ;

    fun isValid(): Boolean = this != INVALID

    fun toRaw(): String = name.lowercase()

    companion object {
        fun asDefault(): ExportFormat = MP3
        fun fromRaw(inputRaw: String?): ExportFormat {
            val _inputRaw = inputRaw ?: return INVALID
            return values().firstOrNull { _inputRaw.equals(it.name, ignoreCase = true) } ?: INVALID
        }
    }
}
