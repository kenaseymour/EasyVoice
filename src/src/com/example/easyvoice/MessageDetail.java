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

import com.example.easyvoice.Message;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MessageDetail extends Activity {
	
	
	private final String DB_NAME = "messagesDb";
	private final String TABLE_NAME = "phrases";
	
	private EditText displayEdit = null;
	private EditText contentEdit = null;
	   
	private Button saveBtn = null;

	private int msgId = -1;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(getClass().getSimpleName(), "onCreate");
		setContentView(R.layout.activity_message_detail);
		
		displayEdit = (EditText) findViewById(R.id.display_edit);
		contentEdit = (EditText) findViewById(R.id.content_edit);
		saveBtn = (Button) findViewById(R.id.save_button);
		
		Log.d(getClass().getSimpleName(), "Checking");
				
		if (displayEdit == null || contentEdit == null) {
			Log.w(getClass().getSimpleName(), "Whoops!");
		}
    }	

	@Override
	protected void onStart() {
		super.onStart ();
		Log.d(getClass().getSimpleName(), "onStart");
	    
		String action = getIntent ().getAction();
		String data = getIntent().getDataString();
		
		if (action.equalsIgnoreCase(Intent.ACTION_EDIT) ) {
			Log.d(getClass().getSimpleName(), "parsing uri, data is "+ data);
			
			
			Uri uri = Uri.parse(data);
			String qp = uri.getQueryParameter("msgid");
			msgId = Integer.parseInt(qp);
			
			Log.d(getClass().getSimpleName(), "Loading msg " + qp);
			
			LoadMessage(msgId);
			
			
			saveBtn.setText(getString(R.string.update_button));
//			saveBtn.setOnClickListener (new View.OnClickListener() {
//				public void onClick(View v) {                 // Perform action on click             }
//					onUpdate(v);
//				}
//			});
		}
			
		
		Log.i(getClass().getSimpleName(), "onStart, action is " + action + ", data is " + data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message_detail, menu);
		return true;
	}
	
	public void LoadMessage (int msgId) {
		
		SQLiteDatabase db = null;
	    try {
	      	db =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
	    	
	      	
	      	ContentValues values = new ContentValues();
	      	values.put("msg_id", msgId);

	      	String[] tableColumns = new String[] {
	      			"msg_id",
	      			"disp_name",
	      			"content"      			
	      	};
	      	String whereClause = "msg_id = ?";
	      	String[] whereArgs = new String[] {
	      	    Integer.toString(msgId)
	      	 };
	      	
	      	Cursor c = db.query(TABLE_NAME, tableColumns, whereClause, whereArgs, null, null, null);

	    	if (c != null ) {
				Log.d(getClass().getSimpleName(), "cursor ok");

	    		
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
    				
    				displayEdit.setText(m.getDispName());
    				contentEdit.setText(m.getContent());
    		/*		
    				map.put (pos, m);
    				
    				results.add(m.getDispName());
   */
    				
    				c.moveToNext(); 			
	    		} 
	    	}
		      	
	      	
	      	
	      	/*
	      	values.put("disp_name", displayEdit.getText ().toString());
	      	values.put("content",   contentEdit.getText ().toString());
	      	*/
	    }
	    catch (SQLiteException se ) {
	    	Log.e(getClass().getSimpleName(), "Could not create or Open the database (2)");
	    }
	}
	
	public void onWrite(View view) {
		String which = getIntent().getAction();
		Log.d(getClass().getSimpleName(), "which is "+ which);
		if (which.equalsIgnoreCase(Intent.ACTION_MAIN)) {
			onSave(view);
		}
		else if (which.equalsIgnoreCase(Intent.ACTION_EDIT)) {
			// TODO set msgId?
			onUpdate (view);
		}
	}
	
	public void onSave(View view) {
		
        Log.i ("MessageDetail", "onSave");
        
        SQLiteDatabase sampleDB = null;
	    try {
	      	sampleDB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
	    	
	        ContentValues values = new ContentValues();
	        values.putNull("msg_id");
	        values.put("disp_name", displayEdit.getText ().toString());
	        values.put("content",   contentEdit.getText ().toString());
	        
	        long insertId = sampleDB.insert(TABLE_NAME, null, values);
	        Log.i(getClass().getSimpleName(), "inserted " + Long.toString(insertId));
	        
	        // invalidate?
	        
	        // go back
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
	    			    		    	
	    } catch (SQLiteException se ) {
	    	Log.e(getClass().getSimpleName(), "Could not create or Open the database");
	    } 
    		
	}
	
	public void onCancel(View view) {
		
        Log.i ("MessageDetail", "onCancel");
        
        // go back
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
	}

	public void onUpdate (View view) {
		Log.i (getClass().getSimpleName(), "onUpdate");
		       
        SQLiteDatabase db = null;
	    try {
	      	db =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
	    	
	        ContentValues values = new ContentValues();
	        
	        values.put("disp_name", displayEdit.getText ().toString());
	        values.put("content",   contentEdit.getText ().toString());
	        
	      	String whereClause = "msg_id = ?";
	      	String[] whereArgs = new String[] {
	      	    Integer.toString(msgId)
	      	 };

	        
	        int count = db.update (TABLE_NAME, values, whereClause, whereArgs);
	        if (count != 1)
	        	Log.w ("expect 1, got ", "onUpdate");
	        // go back
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
	    			    		    	
	    } catch (SQLiteException se ) {
	    	Log.e(getClass().getSimpleName(), "Could not create or Open the database");
	    } 
    		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	
	    	
	    	Log.i(getClass().getSimpleName(), "home pressed");
	    	
	    	
	        Intent upIntent = NavUtils.getParentActivityIntent(this);
	        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
	            // This activity is NOT part of this app's task, so create a new task
	            // when navigating up, with a synthesized back stack.
	            TaskStackBuilder.create(this)
	                    // Add all of this activity's parents to the back stack
	                    .addNextIntentWithParentStack(upIntent)
	                    // Navigate up to the closest parent
	                    .startActivities();
	        } else {
	            // This activity is part of this app's task, so simply
	            // navigate up to the logical parent activity.
	            NavUtils.navigateUpTo(this, upIntent);
	        }
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	  @Override
	  protected void onActivityResult (int requestCode, int resultCode, Intent data)
	  {
	    Log.i(getClass().getSimpleName(), "request " + requestCode + ", result " + resultCode);
	    if (resultCode == Activity.RESULT_OK && requestCode == 0) {
		      String result = data.getDataString();
		      Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	    }
	  }
	  
	  public void Load (Message m) {

//		  EditText displayEdit = (EditText) findViewById(R.id.display_edit);
//		  EditText contentEdit = (EditText) findViewById(R.id.content_edit);

		  displayEdit.setText (m.getDispName(), TextView.BufferType.EDITABLE);
		  contentEdit.setText (m.getContent(), TextView.BufferType.EDITABLE);
	  }
}
