package com.example.easyvoice;

/*
This file is part of Easy Voice.

Easy Voice is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Easy Voice is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Easy Voice.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.database.DataSetObserver;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener
{
	private final String SAMPLE_DB_NAME = "messagesDb";
	private final String SAMPLE_TABLE_NAME = "phrases";

	private TextToSpeech tts;

	private Map<Integer, Message> map;	
	
	private ArrayList<String> listArray = null;
	
	private ArrayAdapter<String> adaptor;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tts = new TextToSpeech(this, this);
		listArray = new ArrayList<String>();adaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listArray);

		adaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listArray);

        adaptor.registerDataSetObserver (new DataSetObserver() {
	    	public void onChanged () {
	    		Log.i(getClass().getSimpleName(), "onChanged");
	    	}
	    	public void onInvalidated () {
	    		Log.i(getClass().getSimpleName(), "onInvalidated");
	    		
	    	}
	    });
	    		
	    final ListView list = (ListView) findViewById (R.id.listView1);
	    
        list.setAdapter (adaptor);
    	
	    
	    OnItemClickListener k1 = new OnItemClickListener() {
	    	 
	    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    		 
    		    //final String item = (String) list.getItemAtPosition(position);
        	    //
    		    Message m = map.get(position);
    		    
        	    String s = m.getContent();
        	    Log.i("onSelect", s);
        	    
        	    if (tts != null) {
        	    	speakOut (s);
        	    }
            }
	    };
	    list.setOnItemClickListener(k1);

	    map = new TreeMap<Integer, Message> ();

	    registerForContextMenu  (list);
	    


	    buildList ();    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.i (getClass().getSimpleName(), "onCreateOptionsMenu");

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    try {
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.cab_menu, menu);
		}
	    catch (InflateException ie ){
	    	Log.e (getClass().getSimpleName(), "onCreateContextMenu", ie);
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    View target = info.targetView;    	
	    switch (item.getItemId()) {
	        case R.id.cab_edit:
	        	try {
	        		if (target instanceof TextView) {
	        			int i = info.position;
	        			Message m = map.get(i);


	        			Intent intent = new Intent(this, MessageDetail.class);
	        			intent.setAction(Intent.ACTION_EDIT);
	        			intent.setData(Uri.parse("content://message?msgid=" + Integer.toString(m.getMsgId())));
	        			Log.d(getClass().getSimpleName(), "edit intent " + intent.getDataString());
	        			startActivity(intent);
	        			
	        			
	        			buildList();
	        		}
	        		Log.i(getClass().getSimpleName(), "edit");
	        	}

	        	catch (RuntimeException rex) {
	        		Log.e(getClass().getSimpleName(), "", rex);
	        		return false;
	        	}
	        	return true;
	        case R.id.cab_delete:
	        	if (target instanceof TextView) {
	        		int i = info.position;
	        		Message m = map.get(i);
	        		String name = ((TextView) target).getText ().toString ();
	        		Log.i(getClass().getSimpleName(), "delete " + name + ", m.name is " + m.getDispName());
	        		doDelete (m);
	     
		        	
	        	} else {
		        	Log.i(getClass().getSimpleName(), "delete something");	        		
	        	}

	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	private void onChange () {
		Log.i(getClass().getSimpleName(), "calling notifyDataSetChanged");
	    adaptor.notifyDataSetChanged();
	}
	
	private void doDelete(Message m) {
		SQLiteDatabase sampleDB = null;
		try
		{
			//   try
			// {
			sampleDB =  this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);
			Log.i(getClass().getSimpleName(), "deleting " + m.getMsgId());
			sampleDB.delete(SAMPLE_TABLE_NAME, "msg_id =?", new String [] { Integer.toString(m.getMsgId()) });
			
		    adaptor.remove (m.getDispName());
		    onChange ();
			
		} catch (SQLiteException se ) {
			Log.e(getClass().getSimpleName(), "Could not create or Open the database");
		}
		finally {

			if (sampleDB != null) 
				sampleDB.close();
		}
	}
      

	@Override
    public void onInit(int status) {
    	 
        if (status == TextToSpeech.SUCCESS) {
 
            int result = tts.setLanguage(Locale.US);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
            	//onLoad(this);
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
        
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i (getClass().getSimpleName(), "onOptionsItemSelected");
        // Handle item selection
        
    	Intent intent = null;
    	
        switch (item.getItemId()) {
            case R.id.add_message:

                intent = new Intent(this, MessageDetail.class);
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
            	
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


	 
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut(String text) {

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void buildList() {
    	
    
       SQLiteDatabase sampleDB = null;
	   int pos = 0;
       listArray.clear();
	   map.clear();
	   try {
	    	sampleDB =  this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);
	    //sampleDB.execSQL("DROP TABLE " + SAMPLE_TABLE_NAME);
	    	sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " +
	    			SAMPLE_TABLE_NAME +
	    			" (msg_id INTEGER PRIMARY KEY AUTOINCREMENT, disp_name VARCHAR, content VARCHAR);");
	    	    	
	    	Cursor c = sampleDB.rawQuery("SELECT rowid, disp_name, content FROM " +
	    			SAMPLE_TABLE_NAME +	"", null);
    	
	    	if (c != null ) {	    		
	    		map.clear();
	   			   		
	    		boolean hasContents = c.moveToFirst();
	    		Log.i(getClass().getSimpleName(), "hasContents is " + hasContents);
	            
	    		while (c.isAfterLast() == false) {
	            	

            		Message m = new Message ();
            		
            		int n = c.getColumnIndex("msg_id");
            		Log.i(getClass().getSimpleName(), "reading column rowid, index is " + n); 
	    				
            		int rowid = c.getInt(n);
            		m.setMsgId(rowid);
            		
    				Log.i(getClass().getSimpleName(), "reading column disp_name");
    				m.setDispName(c.getString(c.getColumnIndex("disp_name")));
    				Log.i(getClass().getSimpleName(), "reading column content");
    				m.setContent(c.getString(c.getColumnIndex("content")));
    				
    				map.put (pos, m);
    				
    				listArray.add(m.getDispName());
   
    				++pos;
    				c.moveToNext();
    			
	    		} 
	    	} 	
            onChange();	    	

	    } catch (SQLiteException se ) {
	    	Log.e(getClass().getSimpleName(), "Could not create or Open the database");

	    } finally {
	    	if (sampleDB != null) 
	    		sampleDB.close();
	    }
        
	}
	
    @Override
    protected void onStart()
    {
    	super.onStart ();
    	Log.i (getClass().getSimpleName(),  "onStart");
    
    }

}