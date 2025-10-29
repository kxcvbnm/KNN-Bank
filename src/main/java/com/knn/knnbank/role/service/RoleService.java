package com.knn.knnbank.role.service;

import java.util.List;

import com.knn.knnbank.response.Response;
import com.knn.knnbank.role.entity.Role;

public interface RoleService {
    
    Response<Role> createRole(Role roleRequest);
   
    Response<Role> updateRole(Role roleRequest);
   
    Response<List<Role>> getAllRole();
   
    Response<?> deleteRole(Long id);
}
