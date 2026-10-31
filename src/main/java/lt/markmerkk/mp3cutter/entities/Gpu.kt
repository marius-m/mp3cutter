package lt.markmerkk.mp3cutter.entities

/**
 * GPU used to encode video when cutting into a video format
 * [NONE] adds no arguments, ffmpeg picks the encoder itself
 */
enum class Gpu(
    /**
     * Arguments that go before input ('-i')
     */
    val globalArgs: List<String>,
    /**
     * Appended to the end of the video filter chain
     */
    val videoFilterSuffix: String,
    /**
     * Video encoder to use, null to let ffmpeg pick
     */
    val videoCodec: String?,
) {
    NONE(
        globalArgs = emptyList(),
        videoFilterSuffix = "",
        videoCodec = null,
    ),

    /**
     * Encodes on AMD GPU through VAAPI
     * Frames are decoded and filtered on CPU, then converted to 'nv12' and uploaded to GPU memory for the encoder
     */
    AMD(
        globalArgs = listOf("-vaapi_device", "/dev/dri/renderD128"),
        videoFilterSuffix = "format=nv12,hwupload",
        videoCodec = "h264_vaapi",
    ),
    ;

    fun toRaw(): String = name.lowercase()

    companion object {
        fun fromRaw(inputRaw: String?): Gpu {
            val _inputRaw = inputRaw ?: return NONE
            return values().firstOrNull { _inputRaw.equals(it.name, ignoreCase = true) } ?: NONE
        }
    }
}
