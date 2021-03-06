package de.patruck.stepaluja;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

public class IOSLauncher extends IOSApplication.Delegate {

    public native static void firebaseInitIOS();

    @Override
    protected IOSApplication createApplication() {

        System.loadLibrary("gnustl_shared");
        System.loadLibrary("FirebaseGameInteractor");

        firebaseInitIOS();

        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        return new IOSApplication(new GameStart(true), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}