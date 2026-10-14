package nl.schnobias.personaltools;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
import android.text.InputFilter;
import com.google.mlkit.genai.prompt.*;
import com.google.mlkit.genai.prompt.java.GenerativeModelFutures;
import com.google.mlkit.genai.common.FeatureStatus;
import com.google.mlkit.genai.common.DownloadCallback;
import com.google.mlkit.genai.common.GenAiException;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.*;

/** Runs Gemini Nano only while the user requests an answer in this foreground screen. */
public final class QuickAnswerActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private GenerativeModelFutures model;
    private ListenableFuture<?> pending;
    private TextView output;
    private EditText input;
    private Button ask;
    private String answer="";
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        model=GenerativeModelFutures.from(Generation.INSTANCE.getClient());
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        int pad=(int)(24*getResources().getDisplayMetrics().density);page.setPadding(pad,pad*2,pad,pad);
        ScrollView scroll=new ScrollView(this);scroll.addView(page);setContentView(scroll);
        TextView title=new TextView(this);title.setText("Quick Answer · on this phone");title.setTextSize(24);page.addView(title);
        input=new EditText(this);input.setHint("Select text in another app, or enter a question here");input.setMinLines(3);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6000)});
        CharSequence selected=getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);if(selected!=null)input.setText(selected.length()>6000?selected.subSequence(0,6000):selected);
        page.addView(input);ask=new Button(this);ask.setText("Quick Answer");page.addView(ask);
        output=new TextView(this);output.setTextSize(17);output.setTextIsSelectable(true);page.addView(output);
        Button copy=new Button(this);copy.setText("Copy answer");page.addView(copy);copy.setOnClickListener(v->{if(!answer.isEmpty())getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Quick Answer",answer));});
        ask.setOnClickListener(v->generate());
        Button prepare=new Button(this);prepare.setText("Prepare on-device model");page.addView(prepare);
        prepare.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Prepare Gemini Nano?").setMessage("AICore may download model files using your connection. Selected text is not sent for cloud inference.").setNegativeButton("Cancel",null).setPositiveButton("Prepare",(d,w)->model.download(new DownloadCallback(){
            public void onDownloadStarted(long bytes){show("Preparing model…",false);}
            public void onDownloadProgress(long bytes){show("Preparing model: "+bytes/1048576+" MB",false);}
            public void onDownloadCompleted(){show("Model ready. Tap Quick Answer.",false);}
            public void onDownloadFailed(GenAiException e){show("Model preparation failed: "+e.getMessage(),false);}
        })).show());
        if(selected!=null && selected.length()>0)generate();
    }
    private void generate(){
        String text=input.getText().toString().trim();if(text.isEmpty()){input.setError("Enter a question or selected text");return;}
        ask.setEnabled(false);answer="";output.setText("Checking on-device model…");
        worker.execute(()->{try{
            ListenableFuture<Integer> status=model.checkStatus();pending=status;int value=status.get(30,TimeUnit.SECONDS);
            if(value!=FeatureStatus.AVAILABLE){show(value==FeatureStatus.DOWNLOADABLE?"Gemini Nano needs to be prepared by AICore. Update Android System Intelligence / AICore and try again when the model is available.":value==FeatureStatus.DOWNLOADING?"Gemini Nano is downloading. Try again once it finishes.":"Gemini Nano is unavailable for this app on this phone. Update AICore and try again.",false);return;}
            runOnUiThread(()->output.setText("Answering locally…"));
            GenerateContentRequest.Builder request=new GenerateContentRequest.Builder(new TextPart("Give a concise helpful answer to the question below. If it is not a question, briefly explain the selected text. Say when you are unsure.\n\n"+text));request.setMaxOutputTokens(256);
            ListenableFuture<GenerateContentResponse> result=model.generateContent(request.build());pending=result;
            GenerateContentResponse response=result.get(90,TimeUnit.SECONDS);
            String reply=response.getCandidates().isEmpty()?"No answer returned. Try a shorter question.":response.getCandidates().get(0).getText();show(reply,true);
        }catch(Exception e){if(pending!=null)pending.cancel(true);show("Could not answer locally: "+(e.getCause()!=null?e.getCause().getMessage():e.getMessage()),false);}});
    }
    private void show(String text,boolean success){runOnUiThread(()->{if(isDestroyed())return;answer=success?text:"";output.setText(text);ask.setEnabled(true);});}
    @Override public void onStop(){super.onStop();if(pending!=null)pending.cancel(true);}
    @Override public void onDestroy(){if(pending!=null)pending.cancel(true);worker.shutdownNow();super.onDestroy();}
}
