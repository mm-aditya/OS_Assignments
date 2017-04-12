package ProgrammingAssignment2;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

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
        SocketAddress sockaddr = new InetSocketAddress("localhost", portNumber);
        try {
            socket.connect(sockaddr);
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
        printer.println("HI");
        byte[] stuff = readAll();   // This is too long?
        printer.println("Cert pls");
        System.out.println("Asking for cert");
        byte[] byteCert = readAll();
        System.out.println(Arrays.toString(byteCert));
        serverCert = getCert(byteCert);
        if ("Meow".equals(new String(DatatypeConverter.printBase64Binary(decryptBytes(stuff)).getBytes()))) {
            printer.println("OK CAN");
        }
        System.out.println("WOOO");
    }

    private X509Certificate getCert(byte[] byteCert) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream stream = new ByteArrayInputStream(byteCert);
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(stream);
        return certificate;
    }

    private byte[] readAll() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
//        byte[] data = new byte[651];
        in.read(data, 0, data.length);
//        while ((nRead = in.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
        buffer.flush();
        return data;
//        return buffer.toByteArray();
    }

//    private byte[] encryptBytes(byte[] toBeEncrypted) throws Exception {
//        Cipher desCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        desCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        return desCipher.doFinal(toBeEncrypted);
//    }

    private byte[] decryptBytes(byte[] toBeDecrypted) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, serverCert.getPublicKey());
        return rsaCipher.doFinal(toBeDecrypted);
    }
}
