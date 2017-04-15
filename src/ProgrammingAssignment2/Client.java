package ProgrammingAssignment2;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
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
    private PrintWriter printer;
    private InputStream in;
    private BufferedReader reader;
    private X509Certificate serverCert;
    private X509Certificate CACert;
    private SecretKey symKey;

    public static void main(String[] args) {
        Client client = new Client("localhost", 6789);
        try {
            client.handshake();
            int numTrial = 10; 
            System.out.println("Pure RSA: small");
            long start = System.currentTimeMillis();
            for (int i = 0; i < numTrial; i++) {
                client.uploadFile("src\\ProgrammingAssignment2\\sampleData\\smallFile.txt", "smallRSA.txt" + i, "RSA/ECB/PKCS1Padding");
                System.out.println(System.currentTimeMillis());
            }
            long total = System.currentTimeMillis() - start;
            long average = total / numTrial;
            System.out.println("Average time: " + average);


//            client.uploadFile("src\\ProgrammingAssignment2\\sampleData\\smallFile.txt", "meow.txt", "AES/ECB/PKCS5Padding");
            System.out.println("Ok uploaded.");
            while (true) ;
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
            byte[] byteSecretKey = decryptBytes(readAll(in), "RSA/ECB/PKCS1Padding", serverCert.getPublicKey());
            getSymKey(byteSecretKey);
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
        return blockCipher(toBeEncrypted, Cipher.ENCRYPT_MODE, cipher);
    }

    private byte[] decryptBytes(byte[] toBeDecrypted, String decryptType, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(decryptType);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return blockCipher(toBeDecrypted, Cipher.DECRYPT_MODE, cipher);
    }

    private void uploadFile(String pathToFile, String name, String encryptionType) throws Exception {
        Key key;
        if (encryptionType.contains("RSA")) key = serverCert.getPublicKey();
        else key = symKey;
        printer.println(encryptionType.substring(0, 3));
        out.write((name + "\n").getBytes());
        File upload = new File(pathToFile);
        BufferedReader reader = new BufferedReader(new FileReader(upload));
        String message;
        String toEncrypt = "";
        while ((message = reader.readLine()) != null) {
            toEncrypt += message + "\n";
        }
        out.write(encryptBytes(toEncrypt.getBytes(), encryptionType, key));
        out.flush();
        waitForServer();
    }

    private void waitForServer() throws Exception {
        socket.setSoTimeout(0);
        String line = reader.readLine();
//        while ((line = reader.readLine()) == null);
        socket.setSoTimeout(10000);
//        if (line.equals("Done!")) {
//            return;
//        }
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
        reader.close();
        socket.close();
    }

    private byte[] blockCipher(byte[] bytes, int mode, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 117 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 117 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i = 0; i < bytes.length; i++) {

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)) {
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn, scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i % length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn, scrambled);

        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }
}
