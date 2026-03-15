package com.sarm.distributed_lovable.workspace_service.service;



import com.sarm.distributed_lovable.common_lib.dto.FileTreeDto;


public interface FileService {


     FileTreeDto getFileTree(Long projectId);

     String getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filepath, String fileContent);

    boolean isUserMemberOfProject(Long projectId, Long userId);
}
