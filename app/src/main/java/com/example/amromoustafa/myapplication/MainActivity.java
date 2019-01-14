package com.example.amromoustafa.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

import org.apache.commons.codec.language.bm.Lang;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText input;
    private ImageButton mic;
    private Spinner spinner;
    //Buttons for translate and speak
    private Button translate;
    private Button speak;
    private static List<String> LanguagesNames = new ArrayList();
    private static List<String> LanguagesValues = new ArrayList();
    private ImageButton play;
    private Language t;
    //TextView to display translated text
    private TextView translatedText;


    private LanguageTranslator translationService;
    private String selectedTargetLanguage = "ar";

    //variable for the Text To Speech service to be used according to the Radio Button options.
    private String speakLanguage;
    private StreamPlayer player = new StreamPlayer();
    private String firstTranslation = "";
    private TextToSpeech textToSpeech;
    private boolean THEVOICE;


   /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (Spinner) findViewById(R.id.spinner);
        try {
            Language t = new Language() {};


            Field fieldlist[] = t.getClass().getFields();
            for (int i = 3; i < fieldlist.length; i++) {
                Field fld = fieldlist[i];
                try {
                    LanguagesNames.add(fld.getName().toLowerCase().substring(0, 1).toUpperCase() + fld.getName().substring(1).toLowerCase());
                    LanguagesValues.add(fld.get(t).toString());
                    String[] stringArray = LanguagesNames.toArray(new String[LanguagesNames.size()]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_spinner_item, stringArray);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(this);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
        }


        translationService = initLanguageTranslatorService();

//        RadioGroup targetLanguage = findViewById(R.id.target_language);
        input = findViewById(R.id.input);
        translate = findViewById(R.id.translate);

        speak = findViewById(R.id.speak);
        translatedText = findViewById(R.id.translated_text);




        //Check if the translate button is clicked. Then check if text is provided to translate.
        translate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Toast.makeText(getApplicationContext(), selectedTargetLanguage, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        selectedTargetLanguage = LanguagesValues.get(position);
        THEVOICE=true;
        switch (position) {
            case 3:
                speakLanguage = SynthesizeOptions.Voice.DE_DE_BIRGITVOICE;
                break;
            case 4:
                speakLanguage = SynthesizeOptions.Voice.ES_ES_LAURAVOICE;
                break;
            case 7:
                speakLanguage = SynthesizeOptions.Voice.FR_FR_RENEEVOICE; //set the speaking language to french
                break;
            case 10:
                speakLanguage = SynthesizeOptions.Voice.IT_IT_FRANCESCAVOICE; //set the speaking language to french
                break;
            case 11:
                speakLanguage = SynthesizeOptions.Voice.JA_JP_EMIVOICE; //set the speaking language to french
                break;
            case 17:
                speakLanguage = SynthesizeOptions.Voice.JA_JP_EMIVOICE; //set the speaking language to french
                break;
            default:
                THEVOICE=false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }


    //Show the translated text in the text view field.
    private void showTranslation(final String translation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translatedText.setText(translation);
            }
        });
    }


    //Use IBM Watson Translator to translate the provided text
    private LanguageTranslator initLanguageTranslatorService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey(getResources().getString(R.string.language_translator_apikey))  //Add your api key here
                .build();

        LanguageTranslator service = new LanguageTranslator("2018-05-01", options);

        service.setEndPoint(getResources().getString(R.string.language_translator_url));  //Add your url here
        return service;
    }

    //Use IBM Text-to-Speech to speak the translated text.
    public void speakup(View view) {

        IamOptions options = new IamOptions.Builder()
                .apiKey(getResources().getString(R.string.text_speech_apikey))   //Add your api key here
                .build();

        textToSpeech = new TextToSpeech(options);

        textToSpeech.setEndPoint(getResources().getString(R.string.text_speech_url));   //Add your url here

        //If no text is entered to translate, prompt the user
        if (firstTranslation.equals("")) {
            Toast.makeText(getApplicationContext(), "Please translate first", Toast.LENGTH_SHORT).show();
        } else {
            if (THEVOICE) {
                new SynthesisTask().execute(firstTranslation);
            }else{

                Toast.makeText(getApplicationContext(), "Text To Speech for this language has not yet been implemented", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //Translate the text entered in the language selected
    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(params[0])
                    .source("en")
                    .target(selectedTargetLanguage)
                    .build();


            TranslationResult result = translationService.translate(translateOptions).execute();
            firstTranslation = result.getTranslations().get(0).getTranslationOutput();
            showTranslation(firstTranslation);
            // use firstTranslation to convert to speech
            return "Did translate";
        }
    }

    //Play the audio for translated word in the language selected
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

    public String aMethod(int arg) {
        String local1 = "a string";
        StringBuilder local2 = new StringBuilder();
        return local2.append(local1).append(arg).toString();
    }

}

