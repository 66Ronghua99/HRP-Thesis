package com.ronghua.bledetect.authentication;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.RequiresApi;

import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.openssl.PEMException;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.io.pem.PemObject;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CsrHelper {
    private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static String CN_PATTERN = "CN=%s, O=KTH, OU=NSS";
    private static final String DEFAULT_RSA_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private HandlerThread handlerThread = new HandlerThread("CSRHandler");
    private Handler handler;
    public final static String PRIVATE = "RSA PRIVATE KEY";
    public final static String PUBLIC = "PUBLIC KEY";
    public final static String CERTIFICATE = "CERTIFICATE";
    private static CsrHelper instance;
    public final static String ALGORITHM = "RSA";
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private KeyPair keypair;
    private PKCS10CertificationRequest csr;
    private X509Certificate certificate;

    public KeyPair getKeypair() {
        return keypair;
    }

    public PKCS10CertificationRequest getCsr() {
        return csr;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        CertificateFactory f = null;
        try {
            f = CertificateFactory.getInstance("X.509");
            this.certificate = (X509Certificate)f.generateCertificate(new ByteArrayInputStream(certificate));
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    public static CsrHelper getInstance(){
        if(instance == null){
            instance = new CsrHelper();
        }
        return instance;
    }

    public boolean isInitialized(){
        return instance != null;
    }

    public CsrHelper generateKeyPair() {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        KeyPairGenerator keyGenerator = null;
        try {
            keyGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGenerator.initialize(2048, new SecureRandom());
        keypair = keyGenerator.generateKeyPair();
        publicKey = keypair.getPublic();
        privateKey = keypair.getPrivate();
        return instance;
    }

    /*
    request server to sign the csr
     */
    private boolean requestSignature(){
        return false;
    }

    public CsrHelper generateCSR(String cn) throws IOException, OperatorCreationException {
        String principal = String.format(CN_PATTERN, cn);

        ContentSigner signer = new JcaContentSignerBuilder(DEFAULT_RSA_SIGNATURE_ALGORITHM).build(keypair.getPrivate());

        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
                new X500Name(principal), keypair.getPublic());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(
                true));
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                extensionsGenerator.generate());
        csr = csrBuilder.build(signer);
        return instance;
    }

    public String getPem(String pemtype) throws Exception {
        String pem = null;
        if(pemtype.equals(PRIVATE)){
            pemWriter(pemtype, privateKey.getEncoded());
        }else if(pemtype.equals(PUBLIC)){
            pemWriter(pemtype, publicKey.getEncoded());
        }else if(pemtype.equals(CERTIFICATE)){
            pemWriter(pemtype, certificate.getEncoded());
        }else{
            throw new PEMException("PEM type is not correct! PEM type: " + pemtype);
        }
        return pem;
    }

    private String pemWriter(String pemtype, byte[] bytes){
        PemObject pemObject = new PemObject(pemtype, bytes);
        StringWriter str = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(str);
        try {
            pemWriter.writeObject(pemObject);
            pemWriter.close();
            str.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getBase64Csr() throws IOException {
        return Base64.getEncoder().encodeToString(csr.getEncoded());
    }
}
