package com.example.taskmon;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CancelReserveActivity extends Activity {

	private int pid;
	private EditText ePid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel_reserve);

		System.loadLibrary("reservationFramework");
		ePid=((EditText)findViewById(R.id.pidInputCancel));

		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pid = Integer.parseInt((ePid.getText().toString())); 
				if (SetReserveActivity.pidMap.containsKey(pid))
				{
					SetReserveActivity.pidMap.remove(pid);
					SetReserveActivity.pidTMap.remove(pid);
				}
				int retVal=cancelReserve(pid);
				if(retVal == 0){
					Toast success=Toast.makeText(getApplicationContext(), "Reservation Cancelled on pid:"+pid, Toast.LENGTH_LONG);
					success.show();
				}
				else{
					Toast failed=Toast.makeText(getApplicationContext(), "Reservation could not be cleaned.", Toast.LENGTH_LONG);
					failed.show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cancel_reserve, menu);
		return true;
	}

	private static native int cancelReserve(int pid);

}
