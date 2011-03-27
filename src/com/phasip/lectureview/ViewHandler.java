package com.phasip.lectureview;

import android.view.View;

public interface ViewHandler<T> {
	public void handle(View v, T object);
}
