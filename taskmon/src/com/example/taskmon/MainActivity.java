package com.example.taskmon;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	public static double startTime;
	public static String debug = "TEAM11";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime= System.currentTimeMillis();
                
        Button setReserveButton = (Button)findViewById(R.id.setReserveButton);
        Button cancelReseveButton = (Button)findViewById(R.id.cancelReserveButton);
        Button viewGraphsButton = (Button)findViewById(R.id.viewGraphs);
        
        setReserveButton.setOnClickListener(new  OnClickListener() {
			
			@Override
			
			public void onClick(View v) {
				Log.w(debug, "Setting Reserve\n");
				Intent setReserveIntent = new Intent(getApplicationContext(),SetReserveActivity.class);
				startActivity(setReserveIntent);
			}
		});
        
        cancelReseveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cancelReserveIntent = new Intent(getApplicationContext(), CancelReserveActivity.class);
				startActivity(cancelReserveIntent);
			}
		});

        viewGraphsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Log.w(debug, "Viewing graphs\n");
				Intent viewGraphsIntent = new Intent(getApplicationContext(), ViewGraphs.class);
				startActivity(viewGraphsIntent);
				
			}
		});
        
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
