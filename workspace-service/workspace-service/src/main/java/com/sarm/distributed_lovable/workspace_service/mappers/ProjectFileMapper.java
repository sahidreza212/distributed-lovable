package com.sarm.distributed_lovable.workspace_service.mappers;


import com.sarm.distributed_lovable.common_lib.dto.FileNode;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
