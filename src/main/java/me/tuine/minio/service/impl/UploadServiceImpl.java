package me.tuine.minio.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import me.tuine.minio.configurer.MinIoUtils;
import me.tuine.minio.service.UploadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final MinIoUtils minIoUtils;

    @Override
    public Map<String, Object> initMultiPartUpload(String path, String filename, Integer partCount, String contentType) {
        path = path.replaceAll("/+", "/");
        if (path.indexOf("/") == 0) {
            path = path.substring(1);
        }
        String filePath = path + "/" + filename;

        Map<String, Object> result;
        if (partCount == 1) {
            String uploadObjectUrl = minIoUtils.getUploadObjectUrl(filePath);
            result = ImmutableMap.of("uploadUrls", ImmutableList.of(uploadObjectUrl));
        } else {
            result = minIoUtils.initMultiPartUpload(filePath, partCount, contentType);
        }

        return result;
    }

    @Override
    public boolean mergeMultipartUpload(String objectName, String uploadId) {
        return minIoUtils.mergeMultipartUpload(objectName, uploadId);
    }
}
