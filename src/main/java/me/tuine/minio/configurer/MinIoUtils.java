package me.tuine.minio.configurer;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.HashMultimap;
import io.minio.http.Method;
import io.minio.messages.Part;
import me.tuine.minio.util.CustomMinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import io.minio.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Configuration
@EnableConfigurationProperties({MinioProperties.class})
public class MinIoUtils {
    private final MinioProperties minioProperties;
    private CustomMinioClient customMinioClient;

    public MinIoUtils(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void init() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccesskey(), minioProperties.getSecretkey())
                .build();
        customMinioClient = new CustomMinioClient(minioClient);
    }

    public String getUploadObjectUrl(String objectName) {
        /*if (StrUtil.isBlank(contentType)) {
            contentType = "application/octet-stream";
        }
        HashMultimap<String, String> headers = HashMultimap.create();
        headers.put("Content-Type", contentType);*/
        try {
            return customMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .expiry(1, TimeUnit.DAYS)
                            //.extraHeaders(headers)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> initMultiPartUpload(String objectName, int partCount, String contentType) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (StrUtil.isBlank(contentType)) {
                contentType = "application/octet-stream";
            }
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type", contentType);
            String uploadId = customMinioClient.initMultiPartUpload(minioProperties.getBucket(), null, objectName, headers, null);

            result.put("uploadId", uploadId);
            List<String> partList = new ArrayList<>();

            Map<String, String> reqParams = new HashMap<>();
            //reqParams.put("response-content-type", "application/json");
            reqParams.put("uploadId", uploadId);
            for (int i = 1; i <= partCount; i++) {
                reqParams.put("partNumber", String.valueOf(i));
                String uploadUrl = customMinioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(minioProperties.getBucket())
                                .object(objectName)
                                .expiry(1, TimeUnit.DAYS)
                                .extraQueryParams(reqParams)
                                .build());
                partList.add(uploadUrl);
            }
            result.put("uploadUrls", partList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public boolean mergeMultipartUpload(String objectName, String uploadId) {
        try {
            Part[] parts = new Part[1000];
            ListPartsResponse partResult = customMinioClient.listMultipart(minioProperties.getBucket(), null, objectName, 1000, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            customMinioClient.mergeMultipartUpload(minioProperties.getBucket(), null, objectName, uploadId, parts, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
