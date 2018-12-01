package de.patruck.stepaluja;

public class NativeBridge
{
    public static final int kAuthErrorEmailAlreadyInUse = 8;

    //public static int initializationPhase = 0;
    public static int errorCode = -2;
    public static String errorMsg = "";

    public native static void firebaseInit();

    public native static void userSignUp(String username, String password);
    public native static int signUpCompleted();
    public native static boolean existsCurrentUser();

    public native static void dispose();

    public native static void registerNewServer(String ipAddress);
    public native static int resultRegisterNewServer();
    public native static void unregisterServer();
    public native static int resultUnregisterServer();

    public native static void matchmakeWithRandom();
    public native static String resultMatchmakeWithRandom();

    public native static void startClientListener();
    public native static void stopClientListener();
    public native static void updateClientListener();
    public native static String getClientListenerResult();

    public native static void registerClient(String ipAddress, String serverUid);
    public native static int registerClientResult(String serverUid);

    public native static void unregisterClient();
    public native static int unregisterClientResult();

    public native static void userSignUpAnonymously();

    public native static int resultSignUpAnonymously();

    public native static void convertAnonymousToPermanentEmailAccount(String username, String password);

    public native static int resultConvertAnonymousToPermanentEmailAccount();
}
