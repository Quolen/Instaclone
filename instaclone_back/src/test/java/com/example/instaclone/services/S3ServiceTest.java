package com.example.instaclone.services;

import com.example.instaclone.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Service underTest;

    @BeforeEach
    void setUp() {
        underTest = new S3Service(s3Client);
    }

    @Test
    void canPutObject() throws IOException {
        // Given
        String bucket = "customer";
        String key = "foo";
        byte[] data = "Hello World".getBytes();

        // When
        underTest.putObject(bucket, key, data);

        // Then
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyArgumentCaptor =
                ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(
                putObjectRequestArgumentCaptor.capture(),
                requestBodyArgumentCaptor.capture()
        );

        PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
        RequestBody requestBody = requestBodyArgumentCaptor.getValue();

        assertThat(putObjectRequest.bucket()).isEqualTo(bucket);
        assertThat(putObjectRequest.key()).isEqualTo(key);
        assertThat(requestBody.contentStreamProvider().newStream().readAllBytes())
                .isEqualTo(RequestBody.fromBytes(data).contentStreamProvider().newStream().readAllBytes());
    }

    @Test
    void canGetObject() {
        // Given
        String bucket = "customer";
        String key = "foo";
        byte[] expectedData = "Hello World".getBytes();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream(expectedData)
        );

        when(s3Client.getObject(getObjectRequest)).thenReturn(responseStream);

        // When
        byte[] result = underTest.getObject(bucket, key);

        // Then
        assertThat(result).isEqualTo(expectedData);
        verify(s3Client).getObject(getObjectRequest);
    }

    @Test
    void canDeleteObject() {
        // Given
        String bucket = "customer";
        String key = "foo";

        // When
        underTest.deleteObject(bucket, key);

        // Then
        ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestArgumentCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);

        verify(s3Client).deleteObject(deleteObjectRequestArgumentCaptor.capture());

        DeleteObjectRequest deleteObjectRequest = deleteObjectRequestArgumentCaptor.getValue();
        assertThat(deleteObjectRequest.bucket()).isEqualTo(bucket);
        assertThat(deleteObjectRequest.key()).isEqualTo(key);
    }
}

