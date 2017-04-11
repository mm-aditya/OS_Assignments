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
            privateKey = getPrivateKey("src\\CSE\\ProgrammingAssignment2\\privateServer.der");
            InputStream certLoc = new FileInputStream("src\\CSE\\ProgrammingAssignment2\\1001522.crt");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            serverCert = (X509Certificate) cf.generateCertificate(certLoc);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void startServer() {
        try {
            server = new ServerSocket(portNum);
            server.setSoTimeout(10000);
        } catch (IOException ioE) {
            System.out.println(ioE.getMessage());
        }
        while (true) {
            try {
                final Socket connection = server.accept();
                Runnable task = () -> handleRequest(connection);
                exec.execute(task);
            } catch (IOException ioE) {
                System.out.println(ioE.getMessage());
            }
        }
    }

    private void handleRequest(Socket socketConnection) {
        try {
            InputStreamReader reader = new InputStreamReader(socketConnection.getInputStream());
            OutputStream writer = socketConnection.getOutputStream();
            BufferedReader buff = new BufferedReader(reader);
            String inLine;
            if ((inLine = buff.readLine()).equals("HI")) {
                writer.write(encryptBytes("Meow".getBytes()));
                writer.flush();
            }
            if ((inLine = buff.readLine()).equals("Cert pls")) {
                writer.write(serverCert.getEncoded());
                writer.flush();
            }
//            if ((inLine = buff.readLine()).equals("OK CAN")) {
//                receiveFile();
//            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void receiveFile() {

    }

    private PrivateKey getPrivateKey(String location) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(location).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey getPublicKey(String filename)
            throws Exception {

        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private byte[] encryptBytes(byte[] toBeEncrypted) {
        try {
//            Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS5Padding");
            Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            desCipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return desCipher.doFinal(toBeEncrypted);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private byte[] decryptBytes(byte[] toBeDecrypted) {
        try {
//            Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS5Padding");
            Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            desCipher.init(Cipher.DECRYPT_MODE, serverCert.getPublicKey());
            return desCipher.doFinal(toBeDecrypted);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
