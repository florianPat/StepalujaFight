package de.patruck.stepaluja;

import android.app.Activity;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;

public class AndroidLauncher extends AndroidApplication
{
	public native static boolean firebaseInitAndroid(Activity activity);

	//TODO: Make firebase connection test!

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		System.loadLibrary("FirebaseGameInteractor");

		// 853 : 480 for 16:9 aspect ratio. But we need a smart camera for that to work!
		final Vector2 worldSize = new Vector2(900.0f, 600.0f);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		if(firebaseInitAndroid(this))
		{
			initialize(new GameStart(worldSize, true), config);
		}
		else
		{
			initialize(new GameStart(worldSize, false), config);
		}
	}
}
