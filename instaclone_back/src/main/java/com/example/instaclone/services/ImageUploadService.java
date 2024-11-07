package com.example.instaclone.services;

import com.example.instaclone.entity.ImageModel;
import com.example.instaclone.entity.Post;
import com.example.instaclone.entity.User;
import com.example.instaclone.exceptions.ImageNotFoundException;
import com.example.instaclone.repository.ImageRepository;
import com.example.instaclone.repository.UserRepository;
import com.example.instaclone.s3.S3Buckets;
import com.example.instaclone.s3.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
@Slf4j
public class ImageUploadService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

    @Autowired
    public ImageUploadService(ImageRepository imageRepository, UserRepository userRepository, S3Service s3Service, S3Buckets s3Buckets) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.s3Buckets = s3Buckets;
    }

    public void uploadImageToUser(MultipartFile file, Principal principal) throws IOException {
        User user = getUserByPrincipal(principal);
        log.info("Uploading image profile to User {}", user.getUsername());

        imageRepository.findByUserIdAndPostId(user.getId(), null).ifPresent(existingImage -> {
            s3Service.deleteObject(s3Buckets.getImgBucket(), existingImage.getS3Key());
            imageRepository.delete(existingImage);
        });

        String s3Key = "profile-images/" + user.getId() + "/" + file.getOriginalFilename();
        log.info("Constructed S3 Key for profile-images: {}", s3Key);
        s3Service.putObject(s3Buckets.getImgBucket(), s3Key, file.getBytes());

        ImageModel image = new ImageModel();
        image.setUserId(user.getId());
        image.setName(file.getOriginalFilename());
        image.setS3Key(s3Key);
        imageRepository.save(image);
    }

    public void deleteProfileImage(Principal principal){
        User user = getUserByPrincipal(principal);
        imageRepository.findByUserIdAndPostId(user.getId(), null).ifPresent(existingImage -> {
            s3Service.deleteObject(s3Buckets.getImgBucket(), existingImage.getS3Key());
            imageRepository.delete(existingImage);
        });
    }

    public void uploadImageToPost(MultipartFile file, Principal principal, Long postId) throws IOException {
        User user = getUserByPrincipal(principal);
        Post post = user.getPosts()
                .stream()
                .filter(p -> p.getId().equals(postId))
                .collect(toSinglePostCollector());

        String s3Key = "post-images/" + postId + "/" + file.getOriginalFilename();
        log.info("Constructed S3 Key for post-images: {}", s3Key);
        s3Service.putObject(s3Buckets.getImgBucket(), s3Key, file.getBytes());

        ImageModel imageModel = new ImageModel();
        imageModel.setPostId(post.getId());
        imageModel.setName(file.getOriginalFilename());
        imageModel.setUserId(user.getId());
        imageModel.setS3Key(s3Key);
        log.info(s3Key);
        log.info("Uploading image profile to Post: {}", postId);

        imageRepository.save(imageModel);
    }

    public ImageModel getImageToUser(Principal principal) {
        User user = getUserByPrincipal(principal);

        ImageModel image = imageRepository.findByUserIdAndPostId(user.getId(), null)
                .orElseThrow(() -> new ImageNotFoundException("Cannot find image for User: " + user.getId()));

        byte[] imageBytes = s3Service.getObject(s3Buckets.getImgBucket(), image.getS3Key());
        image.setImageBytes(imageBytes);

        return image;
    }

    public ImageModel getProfileImageToPost(Long userId) {
        ImageModel image = imageRepository.findByUserIdAndPostId(userId, null)
                .orElseThrow(() -> new ImageNotFoundException("Cannot find profile image for User: " + userId));

        byte[] imageBytes = s3Service.getObject(s3Buckets.getImgBucket(), image.getS3Key());
        image.setImageBytes(imageBytes);

        return image;
    }

    public ImageModel getImageToPost(Long postId) {
        ImageModel image = imageRepository.findByPostId(postId)
                .orElseThrow(() -> new ImageNotFoundException("Cannot find image for Post: " + postId));

        byte[] imageBytes = s3Service.getObject(s3Buckets.getImgBucket(), image.getS3Key());
        image.setImageBytes(imageBytes);

        return image;
    }

    private byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            log.error("Cannot compress bytes");
        }
        System.out.println("Compressed image byte size: " + outputStream.toByteArray().length);
        return outputStream.toByteArray();
    }

    private static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException e) {
            log.error("Cannot decompress bytes");
        }
        return outputStream.toByteArray();
    }

    private <T> Collector<T, ?, T> toSinglePostCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    private User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}
