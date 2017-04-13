package ProgrammingAssignment2;

import javax.crypto.Cipher;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
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
//            Enum<NetworkInterface> meow = NetworkInterface.getNetworkInterfaces();
//            while (meow.hasMoreElements()){
//                System.out.println(NetworkInterface.getNetworkInterfaces().nextElement());
//            }
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
        out.write(encryptBytes(inLine.getBytes(), "RSA/ECB/PKCS1Padding", privateKey));
        inLine = buff.readLine();
        if (inLine.equals("Cert pls")) {
            out.write(serverCert.getEncoded());
            out.flush();
        }
        if (inLine.equals("OK CAN")) {
            receiveFile();
        }
    }

    private void receiveFile() {
        System.out.println("BOOYAH");
    }

    private byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while (true) {
            try {
                nRead = in.read(data, 0, data.length);
                buffer.write(data, 0, nRead);
            } catch (SocketTimeoutException sTimeout) {
                break;
            }
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private PrivateKey getPrivateKey(String location) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(location).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private byte[] encryptBytes(byte[] toBeEncrypted, String encryptType, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(encryptType);
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(toBeEncrypted);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted, String encryptType, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(encryptType);
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        return rsaCipher.doFinal(toBeDecrypted);
    }
}
