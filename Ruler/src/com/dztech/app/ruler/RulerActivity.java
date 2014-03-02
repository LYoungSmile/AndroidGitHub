package com.dztech.app.ruler;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class RulerActivity extends Activity {

	private RulerView rulerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ruler);
		
		rulerView = (RulerView) findViewById(R.id.rulerView);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		rulerView.setYdpi(dm.ydpi);
	}

}
