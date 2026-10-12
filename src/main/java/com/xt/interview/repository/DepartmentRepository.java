package com.xt.interview.repository;

import com.xt.interview.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByDeleted(Integer deleted);
    Optional<Department> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 部门编码唯一性校验。sys_department.code 上是全局 UNIQUE 约束
     * （不像用户名那样带 deleted 条件），所以这里也按全量判断，
     * 否则软删除的记录会绕过校验、直接撞库报错。
     */
    boolean existsByCode(String code);
}
