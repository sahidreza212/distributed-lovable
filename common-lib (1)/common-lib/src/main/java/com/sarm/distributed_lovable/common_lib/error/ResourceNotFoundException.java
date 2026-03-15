package com.sarm.distributed_lovable.common_lib.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@Getter
public class ResourceNotFoundException extends  RuntimeException{
    String resourceName;
    String resourceId;

    public ResourceNotFoundException(String resourceName,String resourceId){
        super(resourceName+" with id "+resourceId+" not found ");
        this.resourceName = resourceName;
        this.resourceId = resourceId;

    }
}
