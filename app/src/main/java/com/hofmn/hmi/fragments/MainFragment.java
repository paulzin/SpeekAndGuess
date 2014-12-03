package com.hofmn.hmi.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hofmn.hmi.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import at.markushi.ui.CircleButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    public static final int MY_DATA_CHECK_CODE = 0;
    private final int REQUEST_CODE = 100;
    private TextToSpeech textToSpeech;
    private TextView myGuess;

    private static int randomNumber;
    private static int guessNumber = -1000;
    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 1;

    private CircleButton guessButton;

    public MainFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeTextToSpeech();
        randomNumber = getRandInt(MIN_VALUE, MAX_VALUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        guessButton = (CircleButton) rootView.findViewById(R.id.guessButton);
        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        myGuess = (TextView) rootView.findViewById(R.id.guessTextView);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int i) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i == TextToSpeech.SUCCESS) {
                                    textToSpeech.setLanguage(Locale.US);
                                    textToSpeech.setPitch(1f);
                                    textToSpeech.setSpeechRate(1.2f);
                                    speak("I'm ready!");
                                } else if (i == TextToSpeech.ERROR) {
                                    Toast.makeText(getActivity(), "Text To Speech failed...",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
                    }
                });
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> resultData = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String resultText = resultData.get(0);
            String text;

            if (isInteger(resultText)) {
                if (guessNumber > randomNumber) {
                    text = resultText + "? No, try a smaller number";
                } else if (guessNumber < randomNumber) {
                    text = resultText + "? No, try a bigger number";
                } else {
                    text = "Yes! It's " + resultText + "!";
                    randomNumber = getRandInt(MIN_VALUE, MAX_VALUE);
                }
            } else {
                text = resultText + " is not a valid number";
            }

            myGuess.setText(text);



            speak(myGuess.getText().toString());
        }
    }

    public static boolean isInteger(String s) {
        try {
            guessNumber = Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Tell me your guess");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    "Speech is not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

    private void initializeTextToSpeech() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public static int getRandInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}
