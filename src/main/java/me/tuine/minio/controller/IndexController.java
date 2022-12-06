package me.tuine.minio.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import me.tuine.minio.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class IndexController {

    private final UploadService uploadService;


    @GetMapping("/hello/welcome")
    public String HelloWorld()
    {
        return "hello";
    }
   @PostMapping("/multipart/init")
    public ResponseEntity<Object> initMultiPartUpload(@RequestBody JSONObject requestParam) {
        String path = requestParam.getStr("path", "test");
        String filename = requestParam.getStr("filename", "test.obj");
        String contentType = requestParam.getStr("contentType", "application/octet-stream");
        String md5 = requestParam.getStr("md5", "");
        Integer partCount = requestParam.getInt("partCount", 1);
        Map<String, Object> result = uploadService.initMultiPartUpload(path, filename, partCount, contentType);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    /*@PostMapping("/multipart/init")
    public ResponseEntity<Object> initMultiPartUpload(@RequestParam(value = "pathtoStore") String pathtoStore, @RequestParam(required = false, name = "fileName") String fileName, @RequestParam("partcount")Integer partcount, @RequestParam( value = "contenttype")String contenttype ) {
        String path = pathtoStore;
       // String filename = requestParam.getStr("filename", "test.obj");
        String filename = fileName;
        String contentType = contenttype;
       // String md5 = requestParam.getStr("md5", "");
        Integer partCount = partcount;
        Map<String, Object> result = uploadService.initMultiPartUpload(path, filename, partCount, contentType);

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }*/


    @PutMapping("/multipart/complete")
    public ResponseEntity<Object> completeMultiPartUpload(
            @RequestBody JSONObject requestParam
    ) {

        String objectName = requestParam.getStr("objectName");
        String uploadId = requestParam.getStr("uploadId");
        Assert.notNull(objectName, "objectName must not be null");
        Assert.notNull(uploadId, "uploadId must not be null");
        boolean result = uploadService.mergeMultipartUpload(objectName, uploadId);

        return new ResponseEntity<>(ImmutableMap.of("success", result), HttpStatus.OK);
    }
}
