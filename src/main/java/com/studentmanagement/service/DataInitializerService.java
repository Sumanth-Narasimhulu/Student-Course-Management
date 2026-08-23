package com.studentmanagement.service;



import org.springframework.stereotype.Service;

import com.studentmanagement.entity.Permission;
import com.studentmanagement.entity.Role;
import com.studentmanagement.repository.PermissionRepository;
import com.studentmanagement.repository.RoleRepository;

import jakarta.transaction.Transactional;

@Service
public class DataInitializerService {
    
    private  RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
    public DataInitializerService(RoleRepository roleRepository, PermissionRepository permissionRepository){
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }
    @Transactional
    public void initializeData(){
        Permission userRead = createPermission("USER_READ");
        Permission userCreate = createPermission("USER_CREATE");
        Permission userUpdate = createPermission("USER_UPDATE");
        Permission userDelete = createPermission("USER_DELETE");

        Role adminRole = createRole("ADMIN");
        Role userRole = createRole("USER");
        adminRole.getPermissions().add(userCreate);
        adminRole.getPermissions().add(userDelete);
        adminRole.getPermissions().add(userUpdate);
        adminRole.getPermissions().add(userRead);

        userRole.getPermissions().add(userRead);

        roleRepository.save(adminRole);
        roleRepository.save(userRole);

        

    }
    public Permission createPermission(String permissionName){
        return permissionRepository.findByName(permissionName)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    return permissionRepository.save(permission);
                });
    }
    public Role createRole(String roleName){
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }
    
}
