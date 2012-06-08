package com.phasip.lectureview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class MarketDialog implements DialogInterface.OnClickListener {
	private static final String prefName = "MarketDialog";
	AlertDialog alert;
	final String app;
	CheckBox notAgain;
	boolean show = false;
	final String id;
	final Activity c;
	final String text;
	private static boolean showAgain(Context c,String id) {
		final SharedPreferences settings = c.getSharedPreferences(prefName, 0);
		if (settings.getBoolean(id, true))
		{
			//Has been show
			return true;
		}
		return false;
	}
	private static void setShowAgain(Context c,String id,boolean again) {
		final SharedPreferences settings = c.getSharedPreferences(prefName, 0);
		Editor e = settings.edit();
		e.putBoolean(id, again);
		e.commit();
	}
	
	public MarketDialog(Activity c,String text,String app,String id) {
		this.c = c;
		this.id = id;
		this.app = app;
		this.text= text;
		if (!showAgain(c,id)) {
			this.show = false;
			return;
		}
		this.show = true;
		

	}
	public boolean show() {
		if (this.show = false)
			return false;
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		LayoutInflater inflater = c.getLayoutInflater();
		View checkboxLayout = inflater.inflate(R.layout.checkboxdialog, null);
		notAgain = (CheckBox)checkboxLayout.findViewById(R.id.checkBox);

		builder.setView(checkboxLayout);
		builder.setTitle("Suggested Application");
		builder.setMessage(text);
		builder.setNeutralButton("Go to market",this);
		builder.setNegativeButton("Cancel",this);
		alert = builder.create();
		
		alert.show();
		return true;
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		this.show = true;
		if (notAgain.isChecked()) {
			setShowAgain(c,id,false);
		}
		if (DialogInterface.BUTTON_POSITIVE != which)
			return;
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pname:" + app));
	    c.startActivity(intent);
	}
}
