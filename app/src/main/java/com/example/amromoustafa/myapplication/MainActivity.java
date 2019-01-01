package com.example.amromoustafa.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.language_translator.v3.util.Language;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

public class MainActivity extends AppCompatActivity {
    private EditText input;
    private ImageButton mic;
    private Button translate;
    private Button speak;
    private ImageButton play;
    private TextView translatedText;

    private LanguageTranslator translationService;
    private String selectedTargetLanguage = Language.SPANISH;

    //variable for the Text To Speech service to be used according to the Radio Button options.
    private String speakLanguage;
    private StreamPlayer player = new StreamPlayer();
    //private View speak;
    private String firstTranslation = "";
    private TextToSpeech textToSpeech;


    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        translationService = initLanguageTranslatorService();

        RadioGroup targetLanguage = findViewById(R.id.target_language);
        input = findViewById(R.id.input);
//        mic = findViewById(R.id.mic);
        translate = findViewById(R.id.translate);

        speak = findViewById(R.id.speak);
//        play = findViewById(R.id.play);
        translatedText = findViewById(R.id.translated_text);

        targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.spanish:
                        selectedTargetLanguage = Language.SPANISH;
                        speakLanguage = SynthesizeOptions.Voice.ES_ES_LAURAVOICE; //set the speaking language to spanish

                        break;
                    case R.id.french:
                        selectedTargetLanguage = Language.FRENCH;
                        speakLanguage = SynthesizeOptions.Voice.FR_FR_RENEEVOICE; //set the speaking language to french
                        break;
                    case R.id.italian:
                        selectedTargetLanguage = Language.ITALIAN;
                        speakLanguage = SynthesizeOptions.Voice.IT_IT_FRANCESCAVOICE; //set the speaking language to italian
                        break;
                }
            }
        });


        translate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (input.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter something to translate", Toast.LENGTH_SHORT).show();
                } else {
                    new TranslationTask().execute(input.getText().toString());
                }
            }
        });

        speak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });


    }

    private void showTranslation(final String translation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translatedText.setText(translation);
            }
        });
    }


    private LanguageTranslator initLanguageTranslatorService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey("39wIftedETesYusnk2LYpXcmWcgoCcGTC4iIXFC3-FFG")
                .build();

        LanguageTranslator service = new LanguageTranslator("2018-05-01", options);

        service.setEndPoint("https://gateway-wdc.watsonplatform.net/language-translator/api");
        return service;
    }

    public void speakup(View view) {
        IamOptions options = new IamOptions.Builder()
                .apiKey("4Bk_bMAbeOB_kLsElJkpnhpgOZHaXggZ82EZSs8iU2Q6")
                .build();

        textToSpeech = new TextToSpeech(options);

        textToSpeech.setEndPoint("https://gateway-syd.watsonplatform.net/text-to-speech/api");
        if (firstTranslation.equals("")) {
            Toast.makeText(getApplicationContext(), "Please translate first", Toast.LENGTH_SHORT).show();
        } else {
            new SynthesisTask().execute(firstTranslation);
        }
    }

    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(params[0])
                    .source(Language.ENGLISH)
                    .target(selectedTargetLanguage)
                    .build();


            TranslationResult result = translationService.translate(translateOptions).execute();
            firstTranslation = result.getTranslations().get(0).getTranslationOutput();
            showTranslation(firstTranslation);
            // use firstTranslation to convert to speech
            return "Did translate";
        }
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                    .text(params[0])
                    .voice(speakLanguage)
                    .accept(SynthesizeOptions.Accept.AUDIO_WAV)
                    .build();
            player.playStream(textToSpeech.synthesize(synthesizeOptions).execute());
            return "Did synthesize";
        }
    }


}

