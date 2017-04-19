package ProgrammingAssignment2;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by HanWei on 11/4/2017.
 */
public class Client {
    int portNumber;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private X509Certificate serverCert;
    private X509Certificate CACert;
    private SecretKey symKey;

    public static void main(String[] args) {
        Client client = new Client("localhost", 6789);
        try {
            client.handshake();
            int numTrial = 10;
//            File root = new File("src\\ProgrammingAssignment2\\sampleData");
//            String name;
//            for (File child :
//                    root.listFiles()) {
//                name = child.getName();
//                System.out.println("RSA: " + name);
//                client.testEncryption(numTrial, "RSA", child.getPath(), name);
//                System.out.println("AES" + name);
//                client.testEncryption(numTrial, "AES", child.getPath(), name);
//            }
            client.testEncryption(1, "RSA", "src\\ProgrammingAssignment2\\sampleData\\largeFile.txt", "largeFile.txt");

            System.out.println("Ok all done.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Client(String ipAddress, int portNum) {
        portNumber = portNum;
        socket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress(ipAddress, portNumber);    // set this to IP address of server
        try {
            socket.connect(sockaddr);
            socket.setSoTimeout(100);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CACert = (X509Certificate) cf.generateCertificate(new FileInputStream("src\\ProgrammingAssignment2\\CA.crt"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handshake() throws Exception {
        String cNonce = generateCnonce();
        out.write(cNonce.getBytes());
        byte[] encryptedCnonce = waitForResponse(in);
        out.write("Cert pls".getBytes());
        System.out.println("Asking for cert");
        byte[] byteCert = waitForResponse(in);
        serverCert = getCert(byteCert);
        if (verifyServer(cNonce, encryptedCnonce, serverCert.getPublicKey())) {
            out.write("OK CAN".getBytes());
            byte[] byteSecretKey = decryptBytes(waitForResponse(in), "RSA/ECB/PKCS1Padding", serverCert.getPublicKey());
            getSymKey(byteSecretKey);
        } else {
            System.out.println("Oh shiet");
        }
    }

    private boolean verifyServer(String cNonce, byte[] encryptedCnonce, Key key) throws Exception {
        return cNonce.equals(new String(decryptBytes(encryptedCnonce, "RSA/ECB/PKCS1Padding", key)));
    }

    private X509Certificate getCert(byte[] byteCert) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream stream = new ByteArrayInputStream(byteCert);
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
        stream.close();
        verifyCert(certificate);
        return certificate;
    }

    private void verifyCert(X509Certificate certificate) throws Exception {
        certificate.checkValidity();
        certificate.verify(CACert.getPublicKey());
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

    private byte[] encryptBytes(byte[] toBeEncrypted, String encryptType, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(encryptType);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        if (encryptType.contains("AES")) return cipher.doFinal(toBeEncrypted);
        return SecStore.blockCipher(toBeEncrypted, Cipher.ENCRYPT_MODE, cipher);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted, String decryptType, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(decryptType);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return SecStore.blockCipher(toBeDecrypted, Cipher.DECRYPT_MODE, cipher);
    }

    private void uploadFile(String pathToFile, String name, String encryptionType) throws Exception {
        Key key;
        if (encryptionType.contains("RSA")) key = serverCert.getPublicKey();
        else key = symKey;
        File upload = new File(pathToFile);
        byte[] toSend = encryptBytes(Files.readAllBytes(upload.toPath()), encryptionType, key);
        System.out.println("Size: " + toSend.length);
        out.write(encryptionType.substring(0, 3).getBytes());
        waitForResponse(in);
        out.write((name).getBytes());
        waitForResponse(in);
        out.write(toSend);
        out.flush();
        waitForServer();
    }

    private byte[] waitForResponse(InputStream in) throws Exception {
        byte[] data = new byte[0];
        while (data.length == 0) {
            data = readAll(in);
        }
        return data;
    }

    private void waitForServer() throws Exception {
        String line = new String(waitForResponse(in));
        if (line.equals("Done!")) System.out.println("Received done");
        else System.out.println("Cannot receive Done");
    }

    private void getSymKey(byte[] encodedKey) throws NoSuchAlgorithmException {
        symKey = new SecretKeySpec(encodedKey, "AES");
    }

    private String generateCnonce() {
        return new BigInteger(50, new SecureRandom()).toString();
    }

    public void closeConnection() throws Exception {
        in.close();
        out.close();
        socket.close();
    }

    private void testEncryption(int numTrial, String RSAAES, String path, String fileName) throws Exception {
        String encryption;
        String[] name = fileName.split("\\.");
        if (RSAAES.equals("RSA")) encryption = "RSA/ECB/PKCS1Padding";
        else if (RSAAES.equals("AES")) encryption = "AES/ECB/PKCS5Padding";
        else return;
        FileWriter writer = new FileWriter("PA2Saved\\Timings.csv", true);
        writer.append(fileName + ",");
        long total = 0;
        long trialTiming;
        long startTrial;
        for (int i = 0; i < numTrial; i++) {
            startTrial = System.currentTimeMillis();
            uploadFile(path, name[0] + (i + 1) + "." + name[1], encryption);
            trialTiming = System.currentTimeMillis() - startTrial;
            System.out.println(trialTiming);
            writer.append("" + trialTiming + ",");
            total += trialTiming;
        }
        long average = total / numTrial;
        writer.write("" + average + "\n");
        writer.flush();
        writer.close();
        System.out.println("Average time: " + average);
    }
}
