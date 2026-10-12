package com.xt.interview.repository;

import com.xt.interview.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByDeleted(Integer deleted);
    Optional<Account> findByIdAndDeleted(Long id, Integer deleted);
    Optional<Account> findByUsernameAndDeleted(String username, Integer deleted);
    Optional<Account> findByUsernameAndPasswordAndDeleted(String username, String password, Integer deleted);
    boolean existsByUsernameAndDeleted(String username, Integer deleted);

    /**
     * 用户列表的搜索 + 分页查询。三个筛选条件都是可选的：
     * 传 null 表示该条件不参与过滤，所以写成 ":param IS NULL OR ..." 的形式，
     * 而不是拼 8 种组合的派生查询方法。
     * <p>
     * usernamePattern 由 Service 层拼好 "%关键字%"（含通配符转义）后传进来，
     * 这里只负责匹配；count 查询由 Spring Data 依据本方法自动推导。
     */
    @Query("""
            SELECT a FROM Account a
            WHERE a.deleted = :deleted
              AND (:usernamePattern IS NULL OR a.username LIKE :usernamePattern ESCAPE '\\')
              AND (:status IS NULL OR a.status = :status)
              AND (:roleLevel IS NULL OR a.roleLevel = :roleLevel)
            """)
    Page<Account> searchAccounts(@Param("deleted") Integer deleted,
                                 @Param("usernamePattern") String usernamePattern,
                                 @Param("status") Integer status,
                                 @Param("roleLevel") Integer roleLevel,
                                 Pageable pageable);
}
