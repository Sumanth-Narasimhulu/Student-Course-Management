package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.dto.request.DepartmentRequest;
import com.studentmanagement.dto.response.AllDepartmentResponse;
import com.studentmanagement.dto.response.DepartmentResponse;
import com.studentmanagement.entity.Department;
import com.studentmanagement.exception.ResourceExistException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.specification.DepartmentSpecification;



@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ResourceExistException("Department with this name already exists " + request.getName());
        }
        Department department = new Department();
        department.setName(request.getName());
        Department savedDepartment = departmentRepository.save(department);
        DepartmentResponse response = new DepartmentResponse();
        response.setId(savedDepartment.getId());
        response.setName(savedDepartment.getName());
        return response;

    }

    @Transactional 
    public DepartmentResponse updateDepartment(Long id,DepartmentRequest request){
        Department department = departmentRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("department not found")
        );
        if(request.getClass()!=null){
            department.setName(request.getName());
        }
        Department savedDepartment = departmentRepository.save(department);
        DepartmentResponse response = new DepartmentResponse();
        response.setId(savedDepartment.getId());
        response.setName(savedDepartment.getName());
        return response;

    }

    @Transactional (readOnly = true)
    public AllDepartmentResponse getAllDepartments(List<Long>ids,List<String>names,Pageable pageable){

        
        Specification<Department>specification = DepartmentSpecification.hasIdsOrHasNames(ids, names);

        Page<Department>page = departmentRepository.findAll(specification,pageable);

        AllDepartmentResponse response = new AllDepartmentResponse();
        List<DepartmentResponse>departmentResponsesList = new ArrayList<>();
        for(Department ele:page.getContent()){
            DepartmentResponse departmentResponse = new DepartmentResponse();
            departmentResponse.setId(ele.getId());
            departmentResponse.setName(ele.getName());
            departmentResponsesList.add(departmentResponse);
        }
        response.setDepartments(departmentResponsesList);
        response.setPage(Long.valueOf(page.getNumber()));
        response.setSize(Long.valueOf(page.getSize()));
        response.setTotalPages(Long.valueOf(page.getTotalPages()));
        response.setTotalElements(Long.valueOf(page.getTotalElements()));
        response.setTotalNumberOfElements(Long.valueOf(page.getNumberOfElements()));
        return response;




    }

    public  String delete(Long id) {
        

        Department department = departmentRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("department not found with id "+id)
        );
        department.setIsDeleted(true);
        departmentRepository.save(department);
        return "Successfully deleted";
    }






}


