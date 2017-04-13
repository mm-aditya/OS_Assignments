package ProgrammingAssignment2;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.Key;
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
    private PrintWriter printer;
    private InputStream in;
    private BufferedReader reader;
    private X509Certificate serverCert;
    private X509Certificate CACert;

    public static void main(String[] args) {
        Client client = new Client(6789);
        try {
            client.handshake();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public Client(int portNum) {
        portNumber = portNum;
        socket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress("localhost", portNumber);    // set this to IP address of server
        try {
            socket.connect(sockaddr);
            socket.setSoTimeout(10000);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CACert = (X509Certificate) cf.generateCertificate(new FileInputStream("src\\ProgrammingAssignment2\\CA.crt"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        printer = new PrintWriter(out, true);
        reader = new BufferedReader(new InputStreamReader(in));
    }

    private void handshake() throws Exception {
        String cNonce = generateCnonce();
        printer.println(cNonce);
        byte[] encryptedCnonce = readAll(in);
        printer.println("Cert pls");
        System.out.println("Asking for cert");
        byte[] byteCert = readAll(in);
        serverCert = getCert(byteCert);
        if (verifyServer(cNonce, encryptedCnonce, serverCert.getPublicKey())) {
            printer.println("OK CAN");
            System.out.println("YAY");
        }
    }

    private boolean verifyServer(String cNonce, byte[] encryptedCnonce, Key key) throws Exception{
        return cNonce.equals(new String(decryptBytes(encryptedCnonce, "RSA/ECB/PKCS1Padding", key)));
    }

    private X509Certificate getCert(byte[] byteCert) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream stream = new ByteArrayInputStream(byteCert);
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
        return certificate;
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
        Cipher rsaCipher = Cipher.getInstance(encryptType);
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(toBeEncrypted);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted, String encryptType, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance(encryptType);
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        return rsaCipher.doFinal(toBeDecrypted);
    }

    private String generateCnonce() {
        return new BigInteger(50, new SecureRandom()).toString();
    }
}
