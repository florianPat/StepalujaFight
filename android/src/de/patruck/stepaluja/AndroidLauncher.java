package de.patruck.stepaluja;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication implements PermissionQuery
{
	private final int MY_PERMISSION_REQUEST = 1;
	private int permission = 0;
	private GameStart gameStart = null;

	public native static boolean firebaseInitAndroid(Activity activity);

	@Override
	public boolean isPermissonGranted()
	{
		return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED);
	}

	@Override
	public void requestPermission()
	{
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
				MY_PERMISSION_REQUEST);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults)
	{
		Utils.aassert(requestCode == MY_PERMISSION_REQUEST);
		// If request is cancelled, the result arrays are empty.
		if(grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			gameStart.nearbyPermissionResult(true);
			permission = 1;
		}
		else
		{
			gameStart.nearbyPermissionResult(false);
			permission = 0;
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        System.loadLibrary("gnustl_shared");
		System.loadLibrary("FirebaseGameInteractor");

		if(isPermissonGranted())
			permission = 1;

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		if(firebaseInitAndroid(this))
		{
			gameStart = new GameStart(true, permission == 1, this);
			initialize(gameStart, config);
		}
		else
		{
			gameStart = new GameStart(false, permission == 1, this);
			initialize(gameStart, config);
		}
	}
}
