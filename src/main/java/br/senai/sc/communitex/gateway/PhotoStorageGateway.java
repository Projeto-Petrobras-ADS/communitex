package br.senai.sc.communitex.gateway;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageGateway {

    String upload(MultipartFile file, String folder);

    void delete(String photoUrl);

    boolean exists(String photoUrl);
}
