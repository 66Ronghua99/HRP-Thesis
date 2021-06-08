package com.ronghua.caserver.controller;

import com.ronghua.caserver.entity.CertEntity;
import com.ronghua.caserver.msgbody.SignRequest;
import com.ronghua.caserver.msgbody.SignRequestVerified;
import com.ronghua.caserver.msgbody.SignResponse;
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
    public ResponseEntity<SignResponse> signRequest(@Valid @RequestBody SignRequestVerified requestBody) throws ExecutionException, InterruptedException {
        System.out.println("enter csr controller");
        Future<SignResponse> futureResult = caService.signCertificate(requestBody);
        SignResponse response = futureResult.get();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/csr/auth")
    public void signRequest(@Valid @RequestBody SignRequest request){
        System.out.println("enter verified controller");
        caService.accountVerify(request);
    }

    @PostMapping(value = "name")
    public ResponseEntity<CertEntity> getCrt(@RequestBody Map<String, String> map){
        String username = map.get("username");
        CertEntity entity = caService.getCertByName(username);
        return ResponseEntity.ok().body(entity);
    }

    //check validity
}
