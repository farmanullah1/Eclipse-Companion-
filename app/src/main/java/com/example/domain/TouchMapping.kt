package com.example.domain

enum class EarbudSide(val displayName: String) {
    LEFT("Left"),
    RIGHT("Right")
}

enum class TouchGesture(val displayName: String, val stepNumber: Int) {
    ONE_TAP("One Tap", 1),
    TWO_TAP("Two Tap", 2),
    THREE_TAP("Three Tap", 3),
    TAP_AND_HOLD("Tap & Hold", 4)
}

enum class TouchAction(val displayName: String, val description: String) {
    NO_ACTION("No Action", "Disables touch response for this gesture"),
    ANC_SWITCHING("ANC Switching", "Cycles between Noise Reduction, Transparency, and Off"),
    PLAY_PAUSE("Play / Pause", "Toggles playback of music or media"),
    NEXT_TRACK("Next Track", "Skips forward to the next audio track"),
    PREVIOUS_TRACK("Previous Track", "Jumps back to previous track"),
    VOLUME_UP("Volume +", "Increases audio playback loudness"),
    VOLUME_DOWN("Volume -", "Decreases audio playback loudness"),
    VOICE_ASSISTANT("Voice Assistant", "Summons Google Assistant or device voice helper"),
    GAMING_MODE("Gaming Mode", "Toggles low-latency gaming transmission")
}

data class EarbudTouchConfig(
    val side: EarbudSide,
    val oneTap: TouchAction = TouchAction.NO_ACTION,
    val twoTap: TouchAction = TouchAction.ANC_SWITCHING,
    val threeTap: TouchAction = TouchAction.NO_ACTION,
    val tapAndHold: TouchAction = TouchAction.NO_ACTION
) {
    fun getActionFor(gesture: TouchGesture): TouchAction {
        return when (gesture) {
            TouchGesture.ONE_TAP -> oneTap
            TouchGesture.TWO_TAP -> twoTap
            TouchGesture.THREE_TAP -> threeTap
            TouchGesture.TAP_AND_HOLD -> tapAndHold
        }
    }

    fun withAction(gesture: TouchGesture, action: TouchAction): EarbudTouchConfig {
        return when (gesture) {
            TouchGesture.ONE_TAP -> copy(oneTap = action)
            TouchGesture.TWO_TAP -> copy(twoTap = action)
            TouchGesture.THREE_TAP -> copy(threeTap = action)
            TouchGesture.TAP_AND_HOLD -> copy(tapAndHold = action)
        }
    }

    companion object {
        fun defaultLeft(): EarbudTouchConfig = EarbudTouchConfig(
            side = EarbudSide.LEFT,
            oneTap = TouchAction.NO_ACTION,
            twoTap = TouchAction.ANC_SWITCHING,
            threeTap = TouchAction.PREVIOUS_TRACK,
            tapAndHold = TouchAction.VOICE_ASSISTANT
        )

        fun defaultRight(): EarbudTouchConfig = EarbudTouchConfig(
            side = EarbudSide.RIGHT,
            oneTap = TouchAction.NO_ACTION,
            twoTap = TouchAction.PLAY_PAUSE,
            threeTap = TouchAction.NEXT_TRACK,
            tapAndHold = TouchAction.GAMING_MODE
        )
    }
}

data class DeviceTouchSettings(
    val leftConfig: EarbudTouchConfig = EarbudTouchConfig.defaultLeft(),
    val rightConfig: EarbudTouchConfig = EarbudTouchConfig.defaultRight(),
    val isAppliedToEarbuds: Boolean = false,
    val lastAppliedTimestamp: Long? = null
)
