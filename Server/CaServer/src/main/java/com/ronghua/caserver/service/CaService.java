package com.ronghua.caserver.service;

import com.ronghua.caserver.dao.CertMapper;
import com.ronghua.caserver.msgbody.SignReqResp;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.Certificate;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.bc.BcRSAContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CaService {
//    @Autowired
//    private CertMapper certDao;
    private static  PrivateKey privateKey;
    private static X509Certificate certificate;
    private long bigInteger = 1;
    private final Lock lock = new ReentrantLock();
    static {
        System.out.println("initialization of cas");
        try {
            privateKey = getPrivateKeyFromKeyFile("ca.der");
            certificate = getCertFromKeyFile("ca.crt");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | CertificateException e) {
            e.printStackTrace();
        }

    }

    @Async("certificateExecutor")
    public Future<SignReqResp> signCertificate(SignReqResp request){
        System.out.println("Service is called");
        PKCS10CertificationRequest csr = null;
        X509Certificate crt = null;
        SignReqResp response =  new SignReqResp();
        try {
            csr = getCsrFromBase64(request.getEncodedCsr());
            crt = sign(csr, privateKey);
            System.out.println(crt.getEncoded().length);
            response.setEncodedCsr(Base64.getEncoder().encodeToString(crt.getEncoded()));
        } catch (IOException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>(response);
    }

    private PKCS10CertificationRequest getCsrFromBase64(String code) throws IOException {
        System.out.println(code);
        byte[] csrStr = Base64.getDecoder().decode(code);
        return new PKCS10CertificationRequest(csrStr);
    }

    private X509Certificate sign(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate)
            throws  IOException, OperatorCreationException, CertificateException {

        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder()
                .find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder()
                .find(sigAlgId);

        AsymmetricKeyParameter foo = PrivateKeyFactory.createKey(caPrivate
                .getEncoded());
        SubjectPublicKeyInfo keyInfo = inputCSR.getSubjectPublicKeyInfo();

//        PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(inputCSR);
        //in newer version of BC such as 1.51, this is
        //PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(inputCSR);
        X509v3CertificateBuilder myCertificateGenerator = null;
        synchronized (lock) {
            myCertificateGenerator = new X509v3CertificateBuilder(
                    new X500Name("CN=issuer"), new BigInteger(String.valueOf(bigInteger)), new Date(
                    System.currentTimeMillis()), new Date(
                    System.currentTimeMillis() + 60 * 1000),
                    inputCSR.getSubject(), keyInfo);
            bigInteger++;
            lock.notifyAll();
        }

        ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(foo);

        X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
        Certificate eeX509CertificateStructure = holder.toASN1Structure();
        //in newer version of BC such as 1.51, this is
        //org.spongycastle.asn1.x509.Certificate eeX509CertificateStructure = holder.toASN1Structure();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Read Certificate
        InputStream is = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
        X509Certificate theCert = (X509Certificate) cf.generateCertificate(is);
        is.close();

        return theCert;
        //return null;
    }

    private static PrivateKey getPrivateKeyFromKeyFile(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = getBytesFromFile(filename);
//        String privatePem = new String(bytes);
//        privatePem = privatePem.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
//        privatePem = privatePem.replace("-----END RSA PRIVATE KEY-----", "");
//        byte [] decoded = Base64.decode(privatePem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);
        return privateKey;
    }


    private static X509Certificate getCertFromKeyFile(String filename) throws IOException, CertificateException {
        String path = ResourceUtils.getURL("classpath:"+filename).getPath();
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(fis);
        return certificate;
    }

    private static byte[] getBytesFromFile(String filename) throws IOException {
        String path = ResourceUtils.getURL("classpath:"+filename).getPath();
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[(int)file.length()];
        dis.readFully(bytes);
        dis.close();
        return bytes;
    }
}
