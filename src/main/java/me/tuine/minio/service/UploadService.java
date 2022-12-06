package me.tuine.minio.service;

import java.util.Map;

public interface UploadService {

    Map<String, Object> initMultiPartUpload(String path, String filename, Integer partCount, String contentType);


     boolean mergeMultipartUpload(String objectName, String uploadId);
}
