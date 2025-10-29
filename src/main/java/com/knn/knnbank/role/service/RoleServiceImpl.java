package com.knn.knnbank.role.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.knn.knnbank.exceptions.BadRequestException;
import com.knn.knnbank.exceptions.NotFoundException;
import com.knn.knnbank.response.Response;
import com.knn.knnbank.role.entity.Role;
import com.knn.knnbank.role.repo.RoleRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepo roleRepo;

    @Override
    public Response<Role> createRole(Role roleRequest) {
        
        if(roleRepo.findByName(roleRequest.getName()).isPresent()) {
            throw new BadRequestException("Role already exists");
        }

        Role savedRole = roleRepo.save(roleRequest);

        return Response.<Role>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("Role created successfully")
            .data(savedRole)
            .build();
    }

    @Override
    public Response<Role> updateRole(Role roleRequest) {
        
        Role role = roleRepo.findById(roleRequest.getId())
            .orElseThrow(() -> new NotFoundException("Role not found"));

        role.setName(roleRequest.getName());

        Role updatedRole = roleRepo.save(role);

        return Response.<Role>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Role updated successfully")
            .data(updatedRole)
            .build();
    }

    @Override
    public Response<List<Role>> getAllRole() {

        List<Role> roles = roleRepo.findAll();

        return Response.<List<Role>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Roles retrieved successfully")
            .data(roles)
            .build();
    }

    @Override
    public Response<?> deleteRole(Long id) {

        if(!roleRepo.existsById(id)) {
            throw new NotFoundException("Role not found");
        }

        roleRepo.deleteById(id);

        return Response.builder()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .message("Role deleted successfully")
            .build();
    }
}
