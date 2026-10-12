package nl.schnobias.personaltools;

import android.app.*;
import android.content.*;
import android.text.InputFilter;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Independent tool: local text storage, no permissions or background tasks. */
final class QuickNotes {
    static final class Note {
        final String id, text;
        Note(String id, String text) { this.id=id; this.text=text; }
    }
    private final Activity activity;
    private final Runnable changed;
    QuickNotes(Activity activity, Runnable changed) { this.activity=activity; this.changed=changed; }
    private SharedPreferences prefs() { return activity.getSharedPreferences("quick_notes",Context.MODE_PRIVATE); }
    List<Note> list() {
        List<Note> result=new ArrayList<>();
        try {
            JSONArray array=new JSONArray(prefs().getString("notes","[]"));
            for(int i=0;i<array.length();i++) { JSONObject item=array.getJSONObject(i); result.add(new Note(item.getString("id"),item.getString("text"))); }
        } catch(JSONException e) { Toast.makeText(activity,"Saved notes could not be read.",Toast.LENGTH_LONG).show(); }
        return result;
    }
    private void save(List<Note> notes) {
        JSONArray array=new JSONArray();
        try { for(Note note:notes) { JSONObject item=new JSONObject(); item.put("id",note.id);item.put("text",note.text);array.put(item); } }
        catch(JSONException e) { return; }
        prefs().edit().putString("notes",array.toString()).apply(); changed.run();
    }
    void edit(Note original) {
        EditText input=new EditText(activity);input.setHint("Write a reminder or reusable snippet…");
        input.setMinLines(4);input.setGravity(android.view.Gravity.TOP);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4000)});
        if(original!=null)input.setText(original.text);
        int pad=(int)(20*activity.getResources().getDisplayMetrics().density);input.setPadding(pad,pad,pad,pad);
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(original==null?"New quick note":"Edit quick note")
                .setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
            String text=input.getText().toString().trim();if(text.isEmpty()){input.setError("Enter some text first");return;}
            List<Note> notes=list();String id=original==null?UUID.randomUUID().toString():original.id;
            if(original!=null)notes.removeIf(note->note.id.equals(id));notes.add(0,new Note(id,text));save(notes);dialog.dismiss();
        }));dialog.show();
    }
    void copy(Note note) {
        activity.getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Quick note",note.text));
        if(android.os.Build.VERSION.SDK_INT<33)Toast.makeText(activity,"Note copied",Toast.LENGTH_SHORT).show();
    }
    void delete(Note note) {
        new AlertDialog.Builder(activity).setTitle("Delete this note?").setMessage("This removes the saved note from this phone.")
                .setNegativeButton("Keep",null).setPositiveButton("Delete",(d,w)->{List<Note> notes=list();notes.removeIf(n->n.id.equals(note.id));save(notes);}).show();
    }
}
