package com.sarm.distributed_lovable.workspace_service.serviceImpl;


import com.sarm.distributed_lovable.common_lib.dto.FileNode;
import com.sarm.distributed_lovable.common_lib.dto.FileTreeDto;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.sarm.distributed_lovable.workspace_service.dto.Project.FileContentResponse;
import com.sarm.distributed_lovable.workspace_service.entity.Project;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectFile;
import com.sarm.distributed_lovable.workspace_service.mappers.ProjectFileMapper;
import com.sarm.distributed_lovable.workspace_service.repository.FileRepository;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.sarm.distributed_lovable.workspace_service.service.FileService;
import io.minio.GetObjectArgs;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private  final MinioClient minioClient;
    private  final ProjectFileMapper projectFileMapper;
    private  final ProjectMemberRepository projectMemberRepository;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    private static final String BUCKET_NAME = "projects";


    @Override
    public FileTreeDto getFileTree(Long projectId) {
        List<ProjectFile>projectFileList = fileRepository.findByProjectId(projectId);
        List<FileNode> projectFileNode = projectFileMapper.toListOfFileNode(projectFileList);
        return new FileTreeDto(projectFileNode);
    }

    @Override
    public String getFileContent(Long projectId, String path) {
        String objectName = projectId + "/" + path;
        try (
                InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .build())) {

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to read file: {}/{}", projectId, path, e);
            throw new RuntimeException("Failed to read file content", e);
        }

    }

    @Override
    @Transactional
    public void saveFile(Long projectId, String path, String content) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("project",projectId.toString()));


        String cleanPath = path.startsWith("/")? path.substring(1):path;
        String objectKey = projectId+"/"+cleanPath;
        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);
            // saving the file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, contentBytes.length, -1)
                            .contentType(determineContentType(path))
                            .build());

            // Saving the metaData
            ProjectFile file =  fileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey) // Use the key we generated
                            .createdAt(Instant.now())
                            .build());

            file.setUpdatedAt(Instant.now());
            fileRepository.save(file);
            log.info("Saved file: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException("File save failed", e);
        }

    }

    @Override
    public boolean isUserMemberOfProject(Long projectId, Long userId) {
        return projectMemberRepository
                .findRoleByProjectIdAndUserId(projectId, userId)
                .isPresent();
    }

    private String determineContentType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type != null) return type;
        if (path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".css")) return "text/css";

        return "text/plain";
    }

}
