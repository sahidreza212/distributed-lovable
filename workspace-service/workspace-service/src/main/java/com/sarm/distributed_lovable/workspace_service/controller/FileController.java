package com.sarm.distributed_lovable.workspace_service.controller;

import com.sarm.distributed_lovable.common_lib.dto.FileTreeDto;
import com.sarm.distributed_lovable.workspace_service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")

public class FileController {

    private  final FileService fileService;

    @GetMapping
    public ResponseEntity<FileTreeDto> getFileTree(@PathVariable Long projectId){
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/content")
    public ResponseEntity<String> getFile(
            @PathVariable Long projectId,
            @RequestParam String path){
         return ResponseEntity.ok(fileService.getFileContent(projectId,path));
    }


}
