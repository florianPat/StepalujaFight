package de.patruck.stepaluja;

import android.app.Activity;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication
{
	public native static boolean firebaseInitAndroid(Activity activity);

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        System.loadLibrary("gnustl_shared");
		System.loadLibrary("FirebaseGameInteractor");

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		if(firebaseInitAndroid(this))
		{
			initialize(new GameStart(true), config);
		}
		else
		{
			initialize(new GameStart(false), config);
		}
	}
}
