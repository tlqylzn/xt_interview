package com.xt.interview.service.impl;

import com.xt.interview.dto.DepartmentDTO;
import com.xt.interview.entity.Department;
import com.xt.interview.repository.DepartmentRepository;
import com.xt.interview.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("部门编码已存在");
        }
        Department department = new Department();
        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setDescription(dto.getDescription());
        department.setStatus(dto.getStatus());
        department.setDeleted(NOT_DELETED);
        department = departmentRepository.save(department);
        return convertToDTO(department);
    }

    @Override
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        if (!department.getCode().equals(dto.getCode()) &&
                departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("部门编码已存在");
        }

        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setDescription(dto.getDescription());
        department.setStatus(dto.getStatus());
        department = departmentRepository.save(department);
        return convertToDTO(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
        department.setDeleted(DELETED);
        departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
        return convertToDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findByDeleted(NOT_DELETED).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setDescription(department.getDescription());
        dto.setStatus(department.getStatus());
        dto.setCreateTime(department.getCreateTime());
        dto.setUpdateTime(department.getUpdateTime());
        return dto;
    }
}
