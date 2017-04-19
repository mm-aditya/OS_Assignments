package ProgrammingAssignment2;

import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
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
        SecretKey symKey;
        byte[] inLine = waitForResponse(socketConnection, in);
        out.write(encryptBytes(inLine, "RSA/ECB/PKCS1Padding", privateKey));
        inLine = waitForResponse(socketConnection, in);
        if (Arrays.equals(inLine, "Cert pls".getBytes())) {
            out.write(serverCert.getEncoded());
            out.flush();
        }
        inLine = waitForResponse(socketConnection, in);
        if (Arrays.equals(inLine, "OK CAN".getBytes())) {
            symKey = getSecretKey();
            out.write(encryptBytes(symKey.getEncoded(), "RSA/ECB/PKCS1Padding", privateKey));
            waitingForUpload(socketConnection, out, in, symKey);
        }
    }

    private byte[] waitForResponse(Socket conn, InputStream in) throws Exception {
        byte[] data = new byte[0];
        while (data.length == 0) {
            data = readAll(conn, in);
        }
        return data;
    }

    private void waitingForUpload(Socket conn, OutputStream out, InputStream in, SecretKey symKey) throws Exception {
        String inLine;
        try {
            while (true) {
                inLine = new String(waitForResponse(conn, in));
                if (inLine.equals("AES")) {
                    receiveFile(conn, out, in, "AES/ECB/PKCS5Padding", symKey);
                } else if (inLine.equals("RSA")) {
                    receiveFile(conn, out, in, "RSA/ECB/PKCS1Padding", privateKey);
                }
            }
        } catch (SocketException se) {
            System.out.println("Socket has closed. Thank you for your patronage.");
            out.close();
            in.close();
            conn.close();
        }
    }

    private void receiveFile(Socket conn, OutputStream out, InputStream in, String decryptType, Key key) throws Exception {
        out.write("K".getBytes());
        String fileName = new String(waitForResponse(conn, in));
        out.write("K".getBytes());
        byte[] toEncrypt = waitForResponse(conn, in);
        FileOutputStream fileWriter = new FileOutputStream("PA2Saved\\" + fileName);
        fileWriter.write(decryptBytes(toEncrypt, decryptType, key));
        fileWriter.close();
        System.out.println("Yey");
        out.write("Done!".getBytes());
    }

    private PrivateKey getPrivateKey(String location) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(location).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        return keyGen.generateKey();
    }

    private byte[] encryptBytes(byte[] toBeEncrypted, String encryptType, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(encryptType);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(toBeEncrypted);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted, String decryptType, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(decryptType);
        cipher.init(Cipher.DECRYPT_MODE, key);
        if (decryptType.contains("AES")) return cipher.doFinal(toBeDecrypted);
        else return blockCipher(toBeDecrypted, Cipher.DECRYPT_MODE, cipher);
    }

    private byte[] readAll(Socket connection, InputStream in) throws Exception {
        connection.setSoTimeout(100);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16777216];
        while (true) {
            try {
                nRead = in.read(data, 0, data.length);
                buffer.write(data, 0, nRead);
            } catch (SocketTimeoutException sTimeout) {
                break;
            }
        }
        connection.setSoTimeout(0);
        return buffer.toByteArray();
    }

    protected static byte[] blockCipher(byte[] bytes, int mode, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, IOException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results

        // toReturn will hold the total result
        ByteArrayOutputStream toReturn = new ByteArrayOutputStream();
        // if we encrypt we use 117 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 117 : 128;
        int count = 0;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        System.out.println("Start encrypt/decrypt");
        while (count < bytes.length) {
            if (count + length > bytes.length) {
                length = bytes.length - count;
                // clean the buffer array
                buffer = new byte[length];
            }
            System.arraycopy(bytes, count, buffer, 0, length);
            toReturn.write(cipher.doFinal(buffer));
            count += length;
        }
        System.out.println("Stop encrypt/decrypt");

        return toReturn.toByteArray();
    }
}
