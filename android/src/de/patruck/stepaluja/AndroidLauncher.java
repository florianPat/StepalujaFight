package de.patruck.stepaluja;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.ByteBuffer;
import java.util.Vector;

public class AndroidLauncher extends AndroidApplication implements PermissionQuery
{
	private final int MY_PERMISSION_REQUEST = 1;
	private int permission = 0;
	private GameStart gameStart = null;
    private String endendpointId = null;

	public native static boolean firebaseInitAndroid(Activity activity);

    private static class ReceiveBytesPayloadListener extends PayloadCallback
    {
        private Vector<byte[]> receiveBytes = new Vector<byte[]>(3);
        private int currentIndex = 0;

        @Override
        public void onPayloadReceived(String endpointId, Payload payload)
        {
            Utils.log("CurrentIndex: " + currentIndex);
            Utils.aassert(currentIndex < 3);
            if(currentIndex >= receiveBytes.size())
                receiveBytes.add(payload.asBytes());
            else
                receiveBytes.set(currentIndex, payload.asBytes());

            ++currentIndex;

            Utils.log("onPayloadReceived");
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update)
        {
            // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
            // after the call to onPayloadReceived().
            Utils.log("onPayloadTransferUpdate");
        }

        public boolean updateIntoBuffer(ByteBuffer byteBuffer)
        {
            Utils.log("updateIntoBuffer");
            if(currentIndex != 0)
            {
                for(int i = currentIndex; i >= 0; --i)
                {
                    byteBuffer.put(receiveBytes.get(i));
                }
                currentIndex = 0;
                return true;
            }

            return false;
        }
    }

    private final ReceiveBytesPayloadListener receiveBytesPayloadListener = new ReceiveBytesPayloadListener();

    private final NearbyAbstraction nearbyAbstraction = new NearbyAbstraction()
    {
        @Override
        public void startAdvertising(String username, char level)
        {
            AdvertisingOptions advertisingOptions =
                    new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();

            Nearby.getConnectionsClient(getContext())
                    .startAdvertising(
                            username + level, "de.patruck.stepaluja", connectionLifecycleCallback, advertisingOptions)
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Utils.log("Started advertiser!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Utils.logBreak("Started advertiser exception: " + e.getMessage(), gameStart);
                        }
                    });
        }

        @Override
        public void startDiscovery()
        {
            DiscoveryOptions discoveryOptions =
                    new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
            Nearby.getConnectionsClient(getContext())
                    .startDiscovery("de.patruck.stepaluja", endpointDiscoveryCallback, discoveryOptions)
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Utils.log("Started discovery!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Utils.logBreak("Started discovery exception: " + e.getMessage(), gameStart);
                        }
                    });
        }

        @Override
        public void stopAdvertising()
        {
            Nearby.getConnectionsClient(getContext()).stopAdvertising();
        }

        @Override
        public void stopDiscovery()
        {
            Nearby.getConnectionsClient(getContext()).stopDiscovery();
        }

        @Override
        public void establishConnection(String endpointId)
        {
            Nearby.getConnectionsClient(getContext())
                    .requestConnection("", endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Utils.log("Request connection!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Utils.logBreak("Request connection exception: " + e.getMessage(), gameStart);
                        }
                    });
        }

        @Override
        public void disconnectFromAllEndpoints()
        {
            Nearby.getConnectionsClient(getContext())
                    .stopAllEndpoints();
        }

        @Override
        public void send(byte[] bytes)
        {
            Utils.aassert(endendpointId != null);
            Payload bytesPayload = Payload.fromBytes(bytes);
            Nearby.getConnectionsClient(getContext()).sendPayload(endendpointId, bytesPayload);
        }

        @Override
        public boolean receive(ByteBuffer bufferBytesRead)
        {
            return receiveBytesPayloadListener.updateIntoBuffer(bufferBytesRead);
        }
    };

    //NOTE: Just connect if you call startAdvertise and one wants to
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback()
    {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo)
        {
            if(nearbyAbstraction.connectedFlag != 1)
            {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(getContext()).acceptConnection(endpointId, receiveBytesPayloadListener);
            }
            else
            {
                Nearby.getConnectionsClient(getContext()).rejectConnection(endpointId);
            }
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result)
        {
            switch(result.getStatus().getStatusCode())
            {
                case ConnectionsStatusCodes.STATUS_OK:
                {
                    nearbyAbstraction.connectedFlag = 1;
                    endendpointId = endpointId;
                    Utils.log("We're connected! Can now start sending and receiving data.");
                    break;
                }
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                {
                    nearbyAbstraction.connectedFlag = -1;
                    Utils.log("The connection was rejected by one or both sides.");
                    endendpointId = null;
                    break;
                }
                case ConnectionsStatusCodes.STATUS_ERROR:
                {
                    nearbyAbstraction.connectedFlag = -2;
                    Utils.logBreak("The connection broke before it was able to be accepted.", gameStart);
                    endendpointId = null;
                    break;
                }
            }
        }

        @Override
        public void onDisconnected(String endpointId)
        {
            Utils.log("We are disconnected!");
            nearbyAbstraction.connectedFlag = 0;
            endendpointId = null;
        }
    };

    //NOTE: Callback for startDiscover
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback()
    {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info)
        {
            Utils.aassert(nearbyAbstraction.listItems != null);

            if(info.getServiceId().equals("de.patruck.stepaluja"))
            {
                String endpointName = info.getEndpointName();

                nearbyAbstraction.listItems.add(new NearbyServerList.ListItem(
                        endpointName.substring(0, endpointName.length() - 1),
                        LevelSelectMenuComponent.getLevelName(endpointName.charAt(endpointName.length() - 1)),
                        endpointId));
            }
        }

        @Override
        public void onEndpointLost(String endpointId)
        {
            Utils.aassert(nearbyAbstraction.listItems != null);

            //NOTE: Maybe use a map here?!
            for(int i = 0; i < nearbyAbstraction.listItems.size(); ++i)
            {
                NearbyServerList.ListItem item = nearbyAbstraction.listItems.elementAt(i);
                if(item.endpointId.equals(endpointId))
                {
                    nearbyAbstraction.listItems.remove(i);
                    return;
                }
            }

            Utils.invalidCodePath();
        }
    };

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
            gameStart = new GameStart(true, permission == 1, this,
                    this.nearbyAbstraction);
			initialize(gameStart, config);
		}
		else
		{
            gameStart = new GameStart(false, permission == 1, this,
                    this.nearbyAbstraction);
			initialize(gameStart, config);
		}
	}
}
