package ProgrammingAssignment2;

import javax.crypto.Cipher;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by HanWei on 11/4/2017.
 */

// when connected, do the handshake protocol
// Wait for file to be uploaded.
// Save file.
// Time the transfer duration (including encrypting and decrypting)
// Usable interface. For example, you may develop a dynamic GUI that shows the changing states of the
// various modules as your “protocol” proceeds over time.
// Monitoring of transfers or diagnosis of transfer problems (e.g., traceroute between client and server).
// Support for anycast upload to distributed servers.
// SSH for security.

public class SecStore {
    private ServerSocket server;
    private final Executor exec;
    private final int portNum;
    private X509Certificate serverCert;
    private PrivateKey privateKey;

    public SecStore(int port, int numThreads) {
        portNum = port;
        exec = Executors.newFixedThreadPool(numThreads);
        try {
            privateKey = getPrivateKey("src\\ProgrammingAssignment2\\privateServer.der");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            serverCert = (X509Certificate) cf.generateCertificate(new FileInputStream("src\\ProgrammingAssignment2\\1001522.crt"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            server = new ServerSocket(portNum);
            while (true) {
                final Socket connection = server.accept();
                Runnable task = () -> {
                    try {
                        handleRequest(connection);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                };
                exec.execute(task);
            }
        } catch (IOException ioE) {
            System.out.println(ioE.getMessage());
            ioE.printStackTrace();
        }
    }

    private void handleRequest(Socket socketConnection) throws Exception {
        OutputStream out = socketConnection.getOutputStream();
        InputStream in = socketConnection.getInputStream();
        System.out.println("Connection established");
        PrintWriter printer = new PrintWriter(out);
        BufferedReader buff = new BufferedReader(new InputStreamReader(in));
        String inLine = buff.readLine();
        if (inLine.equals("HI")) {  // TODO: Still need to do the nonce thing.
            out.write(encryptBytes("Meow".getBytes()));
            out.flush();
        }
        inLine = buff.readLine();
        if (inLine.equals("Cert pls")) {
            out.write(serverCert.getEncoded());
            out.flush();    // reaches here fine
        }
//            if (inLine.equals("OK CAN")) {
//                receiveFile();
//            }
    }

    private void receiveFile() {

    }

    private PrivateKey getPrivateKey(String location) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(location).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private byte[] encryptBytes(byte[] toBeEncrypted) throws Exception {
        Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        desCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return desCipher.doFinal(toBeEncrypted);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted) throws Exception {
        Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        desCipher.init(Cipher.DECRYPT_MODE, serverCert.getPublicKey());
        return desCipher.doFinal(toBeDecrypted);
    }
}
