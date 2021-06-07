package com.ronghua.caserver.controller;

import com.ronghua.caserver.msgbody.SignReqResp;
import com.ronghua.caserver.service.CaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController()
@RequestMapping(value = "/sign", produces = MediaType.APPLICATION_JSON_VALUE)
public class CertificateController {

    @Autowired
    private CaService caService;
    //get certificate

    //post csr to get signed
    @PostMapping("/csr")
    public ResponseEntity<SignReqResp> signRequest(@Valid @RequestBody SignReqResp requestBody) throws ExecutionException, InterruptedException {
        System.out.println("enter csr controller");
        Future<SignReqResp> futureResult = caService.signCertificate(requestBody);
        SignReqResp response = futureResult.get();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping(value = "str")
    public ResponseEntity<String> getStr(){
        System.out.println("enter str controller");
        return ResponseEntity.ok().body("lalala");
    }

    //check validity
}
