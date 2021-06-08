package com.ronghua.caserver.controller;

import com.ronghua.caserver.entity.CertEntity;
import com.ronghua.caserver.msgbody.SignReqResp;
import com.ronghua.caserver.service.CaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController()
@RequestMapping(value = "/crt", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "name")
    public ResponseEntity<CertEntity> getCrt(@RequestBody Map<String, String> map){
        String username = map.get("username");
        CertEntity entity = caService.getCertByName(username);
        return ResponseEntity.ok().body(entity);
    }

    //check validity
}
