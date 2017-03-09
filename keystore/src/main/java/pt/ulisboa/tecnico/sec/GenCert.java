package pt.ulisboa.tecnico.sec;

import sun.security.x509.X509CertInfo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.x509.*;

public class GenCert {

    public static void main(String[] args) throws Exception{
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      keyGen.initialize(2048, random);
      KeyPair pair = keyGen.generateKeyPair();
      PrivateKey priv = pair.getPrivate();
      PublicKey pub = pair.getPublic();
      X509Certificate[] cert = generateCertificate(pair);
      System.out.println("Private: "+priv);
      System.out.println("Public: "+pub);
      System.out.println("Certificate: "+cert);
    //  System.out.println(cert.getPublicKey());
    }

    /*public static void write(Public , String keyPath) throws Exception {
        FileOutputStream fos = new FileOutputStream(keyPath);
        fos.write(key);
        fos.close();
    }*/

    public static X509Certificate[] generateCertificate(KeyPair pair) throws Exception {
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + 365 * 86400000l); //1 year
        CertificateValidity interval = new CertificateValidity(from, to);
        X500Name owner = new X500Name("C=PT, ST=SEC");

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER,owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(pair.getPrivate(), "SHA1withRSA");
        algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(pair.getPrivate(), "SHA1withRSA");
        return new X509Certificate[]{cert};
    }
}