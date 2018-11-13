//package de.patruck.stepaluja;
//
//import com.esotericsoftware.kryonet.*;
//
//public class KyronetTestMain
//{
//    public static void main(String[] args)
//    {
//        Utils.aassert(args.length > 0);
//
//        if(args[0] == "server")
//        {
//            Server server = new Server();
//            server.start();
//            try
//            {
//            server.bind(54555, 54777);
//            }
//            catch(Exception e)
//            {
//                Utils.log("Could not bind the server to a port!");
//            }
//
//            server.addListener(new Listener() {
//                public void received (Connection connection, Object object) {
//                }
//            });
//        }
//        else
//        {
//            Client client = new Client();
//            client.start();
//            try
//            {
//            client.connect(5000, "192.168.0.1", 54555, 54777);
//            }
//            catch(Exception e)
//            {
//                Utils.log("Could not connect to the server!");
//            }
//        }
//    }
//}