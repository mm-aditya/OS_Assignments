package ProgrammingAssignment2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by HanWei on 11/4/2017.
 */
public class Client {
    //    private final Socket socket;
    public static void main(String[] args) throws Exception {
        int portNumber = 4321;
        Socket echoSocket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress("localhost", portNumber);
        echoSocket.connect(sockaddr, 100);
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        InputStreamReader reader = new InputStreamReader(echoSocket.getInputStream());
        BufferedReader in = new BufferedReader(
                        new InputStreamReader(echoSocket.getInputStream()));
        out.print("HI");

    }
}
