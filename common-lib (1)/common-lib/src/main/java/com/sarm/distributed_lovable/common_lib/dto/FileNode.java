package com.sarm.distributed_lovable.common_lib.dto;

public record FileNode(
        String path
) {

    public String toString(){
        return path;
    }

}
