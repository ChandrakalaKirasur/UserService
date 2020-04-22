package com.bridgelabz.fundoonotesms.service;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonS3ClientService
{
    String uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess, String token);

    void deleteFileFromS3Bucket(String fileName);

    String download(String fileName, String token);
}