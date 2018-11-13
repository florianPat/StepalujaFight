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
}
