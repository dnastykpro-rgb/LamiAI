package com.lumi.ai.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class VoiceInputHelper {

    public interface VoiceCallback {
        void onListeningStarted();
        void onRmsChanged(float rmsdB);
        void onResult(String recognizedText);
        void onError(String errorMessage);
    }

    private SpeechRecognizer speechRecognizer;
    private final Intent recognizerIntent;
    private final VoiceCallback callback;

    public VoiceInputHelper(Context context, VoiceCallback callback) {
        this.callback = callback;
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        this.recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        this.recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        this.recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        this.recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        this.speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                callback.onListeningStarted();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {
                callback.onRmsChanged(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                callback.onError("Не удалось распознать речь. Попробуйте еще раз.");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    callback.onResult(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty()) {
                    callback.onResult(partial.get(0));
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}

