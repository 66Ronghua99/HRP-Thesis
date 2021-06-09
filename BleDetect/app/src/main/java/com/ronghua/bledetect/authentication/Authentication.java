package com.ronghua.bledetect.authentication;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Authentication {
    private static Authentication instance;
//    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private HandlerThread handlerThread = new HandlerThread("authentication");
    private Handler handler;
    private CsrHelper selfHelper;
    private volatile X509Certificate CaCrt;
    private Map<Integer, X509Certificate> certificateMap = new ConcurrentHashMap<>();

    public static Authentication getInstance() {
        if(null ==  instance){
            instance = new Authentication();
        }
        return instance;
    }

    private Authentication(){
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        selfHelper = CsrHelper.getInstance();
    }

    private boolean verifyCrt(int id){
        if(CaCrt == null){
            return false;
        }
        X509Certificate crt = certificateMap.get(id);
        PublicKey caKey = CaCrt.getPublicKey();
        try {
            if (crt != null) {
                crt.verify(caKey);
            }else
                return false;
        } catch (CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
            return false;
        }
        return true;
    }

    private PublicKey getPublicKeyFromCertFile(InputStream certfile) throws FileNotFoundException, CertificateException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(certfile);
        Principal subjectDN = certificate.getSubjectDN();
        System.out.println(subjectDN);
        PublicKey publicKey = certificate.getPublicKey();
        return publicKey;
    }

    private PublicKey getPublicKeyFromCrtList(int id){
        X509Certificate crt = certificateMap.get(id);
        return crt != null ? crt.getPublicKey() : null;
    }

    private void addPublicKeyToCrtList(int id, X509Certificate crt){
        certificateMap.put(id, crt);
    }


    public X509Certificate getCaCrt() {
        return CaCrt;
    }

    public void setCaCrt(X509Certificate caCrt) {
        CaCrt = caCrt;
    }


}
