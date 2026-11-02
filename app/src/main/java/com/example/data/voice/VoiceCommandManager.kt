package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.R
import java.util.Locale

class VoiceCommandManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        onStateChange: (VoiceState) -> Unit,
        onRmsChanged: (Float) -> Unit,
        onResult: (VoiceCommandResult) -> Unit
    ) {
        // Ensure running on main thread
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onStateChange(VoiceState.Listening)
                    }

                    override fun onBeginningOfSpeech() {
                        onStateChange(VoiceState.Listening)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsChanged(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        onStateChange(VoiceState.Processing)
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val errorRes = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> R.string.voice_feedback_unknown
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.voice_status_permission_needed
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> R.string.voice_status_listening
                            else -> R.string.voice_feedback_unknown
                        }
                        onStateChange(VoiceState.Error(errorRes, "Speech error code: $error"))
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull() ?: ""
                        if (spoken.isNotBlank()) {
                            val parsed = VoiceCommandParser.parse(spoken)
                            onStateChange(VoiceState.Success(parsed))
                            onResult(parsed)
                        } else {
                            onStateChange(VoiceState.Error(R.string.voice_feedback_unknown))
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            // Can show live partial transcription
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("VoiceCommandManager", "Failed to start SpeechRecognizer", e)
                isListening = false
                onStateChange(VoiceState.Error(R.string.voice_status_not_available, e.message))
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                if (isListening) {
                    speechRecognizer?.stopListening()
                    isListening = false
                }
            } catch (e: Exception) {
                Log.e("VoiceCommandManager", "Error stopping SpeechRecognizer", e)
            }
        }
    }

    fun simulateCommand(
        spokenText: String,
        onStateChange: (VoiceState) -> Unit,
        onResult: (VoiceCommandResult) -> Unit
    ) {
        onStateChange(VoiceState.Processing)
        val parsed = VoiceCommandParser.parse(spokenText)
        onStateChange(VoiceState.Success(parsed))
        onResult(parsed)
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                isListening = false
            } catch (e: Exception) {
                Log.e("VoiceCommandManager", "Error destroying SpeechRecognizer", e)
            }
        }
    }
}
