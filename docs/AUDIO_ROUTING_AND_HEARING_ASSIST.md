# Audio Routing and Hearing Assist Specification

## Architecture Overview
Hearing Assist routes processed microphone input from the Android smartphone directly to connected Bluetooth earbuds with bounded low-latency DSP filtering.

```
[Phone Built-in Mic] -> AudioRecord (16kHz Mono 16-bit PCM)
         ↓
  [Ring Buffer (256 frames)]
         ↓
  [AudioProcessor]
     - 120Hz High-Pass Rumble Filter (IIR Alpha)
     - Speech Clarity Formant Boost (1.35x - 1.75x)
     - Soft Hyperbolic Limiter (Peak clamp at 0.75 threshold)
         ↓
[AudioTrack Stream (Voice Communication Usage)] -> [Bluetooth A2DP/SCO Output]
```

## Route Change Handling
- `AudioRouteMonitor` listens to system `AudioDeviceCallback` events and `ACTION_AUDIO_BECOMING_NOISY`.
- If Bluetooth connection drops or audio switches to speakerphone, the UI displays a route alert and allows immediate one-tap stop.
- Audio focus is managed exclusively via `AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`.
