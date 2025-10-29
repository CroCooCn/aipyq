package com.aipyq.friendapp.service;

import com.aipyq.friendapp.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OssService {
    private final OssProperties props;

    public OssService(OssProperties props) {
        this.props = props;
    }

    public String upload(InputStream in, long size, String originalFilename, String contentType) {
        String key = buildKey(originalFilename);
        OSS client = new OSSClientBuilder().build(props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            if (size > 0) {
                metadata.setContentLength(size);
            }
            if (contentType != null && !contentType.isBlank()) {
                metadata.setContentType(contentType);
            }
            client.putObject(props.getBucket(), key, in, metadata);
            long seconds = props.getSignedUrlExpireSeconds() == null ? 7L * 24 * 3600 : props.getSignedUrlExpireSeconds();
            Date expiration = new Date(System.currentTimeMillis() + seconds * 1000L);
            URL signed = client.generatePresignedUrl(props.getBucket(), key, expiration);
            return signed.toString();
        } finally {
            client.shutdown();
        }
    }

    private String buildKey(String originalFilename) {
        String date = LocalDate.now().toString();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return "uploads/" + date + "/" + UUID.randomUUID() + ext;
    }
}
