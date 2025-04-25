package com.campus.service.impl;

import com.campus.config.StorageProperties;
import com.campus.exception.StorageException;
import com.campus.exception.StorageFileNotFoundException;
import com.campus.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements FileStorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        // 路径不能为空
        if (properties.getLocation() == null || properties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location cannot be empty.");
        }
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    @PostConstruct // 使用 jakarta.annotation.PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Initialized storage directory: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location: " + rootLocation.toAbsolutePath(), e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String... subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }

        // 清理并生成唯一文件名
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 可以加入对文件类型的校验
            // if (!isValidExtension(fileExtension)) { throw new StorageException(...); }

            String filename = UUID.randomUUID() + fileExtension;

            // 解析目标路径（包含子目录）
            Path targetDirectory = this.rootLocation;
            if (subDirectory != null && subDirectory.length > 0) {
                // 过滤无效的子目录名，防止路径遍历攻击
                String cleanSubDirectory = Stream.of(subDirectory)
                        .filter(StringUtils::hasText)
                        .map(s -> s.replaceAll("[^a-zA-Z0-9_\\-/]", "")) // 只允许字母、数字、下划线、连字符、斜杠
                        .findFirst()
                        .orElse("");
                if (StringUtils.hasText(cleanSubDirectory)) {
                    targetDirectory = this.rootLocation.resolve(cleanSubDirectory).normalize();
                    // 确保子目录在根目录下
                    if (!targetDirectory.startsWith(this.rootLocation)) {
                        throw new StorageException("Cannot store file outside current directory.");
                    }
                    Files.createDirectories(targetDirectory); // 如果子目录不存在则创建
                }
            }

            Path destinationFile = targetDirectory.resolve(filename).normalize().toAbsolutePath();

            // 再次确保目标文件在根目录下
            if (!destinationFile.getParent().startsWith(targetDirectory)) {
                throw new StorageException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 返回相对于根目录的路径
            return this.rootLocation.relativize(destinationFile).toString().replace('\\', '/');

        } catch (IOException e) {
            throw new StorageException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1) // 只遍历根目录下的文件和目录
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        // 清理文件名，防止路径遍历
        String cleanFilename = StringUtils.cleanPath(filename).replaceAll("[^a-zA-Z0-9_\\-\\./]", "");
        return rootLocation.resolve(cleanFilename).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public boolean deleteFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }
        try {
            Path fileToDelete = load(relativePath);
            // 安全检查：确保要删除的文件在根目录下
            if (!fileToDelete.startsWith(this.rootLocation) || fileToDelete.equals(this.rootLocation)) {
                System.err.println("Attempted to delete file outside or equal to root directory: " + relativePath);
                return false;
            }
            return Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + relativePath + " due to " + e.getMessage());
            return false;
        }
    }
}