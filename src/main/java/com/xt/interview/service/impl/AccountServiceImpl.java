package com.xt.interview.service.impl;

import com.xt.interview.dto.AccountDTO;
import com.xt.interview.dto.PageResult;
import com.xt.interview.entity.Account;
import com.xt.interview.entity.Department;
import com.xt.interview.repository.AccountRepository;
import com.xt.interview.repository.DepartmentRepository;
import com.xt.interview.service.AccountService;
import com.xt.interview.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    private static final String DEFAULT_PASSWORD = "123456";

    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限，避免前端传个 size=100000 把整张表捞出来 */
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public AccountDTO createAccount(AccountDTO dto) {
        if (accountRepository.existsByUsernameAndDeleted(dto.getUsername(), NOT_DELETED)) {
            throw new RuntimeException("用户名已存在");
        }
        validateDepartmentExists(dto.getDepartmentId());
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setName(dto.getName());
        account.setGender(dto.getGender());
        account.setPhone(dto.getPhone());
        account.setDepartmentId(dto.getDepartmentId());
        String password = (dto.getPassword() == null || dto.getPassword().isEmpty())
                ? DEFAULT_PASSWORD : dto.getPassword();
        account.setPassword(MD5Util.encode(password));
        account.setRoleLevel(dto.getRoleLevel());
        account.setStatus(dto.getStatus());
        account.setIsSuper(dto.getIsSuper());
        account.setDeleted(NOT_DELETED);
        account = accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    public AccountDTO updateAccount(Long id, AccountDTO dto) {
        Account account = accountRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        if (!account.getUsername().equals(dto.getUsername()) &&
            accountRepository.existsByUsernameAndDeleted(dto.getUsername(), NOT_DELETED)) {
            throw new RuntimeException("用户名已存在");
        }

        validateDepartmentExists(dto.getDepartmentId());
        account.setUsername(dto.getUsername());
        account.setName(dto.getName());
        account.setGender(dto.getGender());
        account.setPhone(dto.getPhone());
        account.setDepartmentId(dto.getDepartmentId());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            account.setPassword(MD5Util.encode(dto.getPassword()));
        }
        account.setRoleLevel(dto.getRoleLevel());
        account.setStatus(dto.getStatus());
        account.setIsSuper(dto.getIsSuper());
        account = accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        account.setDeleted(DELETED);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountById(Long id) {
        Account account = accountRepository.findByIdAndDeleted(id, NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        return convertToDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findByDeleted(NOT_DELETED);
        // 一次性把用到的部门查成 id -> name 的映射，避免逐行 convertToDTO 时产生 N+1 查询
        Map<Long, String> departmentNames = loadDepartmentNames(
                accounts.stream().map(Account::getDepartmentId).collect(Collectors.toList()));
        return accounts.stream()
                .map(account -> convertToDTO(account, departmentNames))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AccountDTO> searchAccounts(String username, Integer status, Integer roleLevel,
                                                 int page, int size) {
        // 页码/页大小直接来自请求参数，先夹到合法范围，再交给数据库
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Account> result = accountRepository.searchAccounts(
                NOT_DELETED, buildUsernamePattern(username), status, roleLevel, pageable);

        Map<Long, String> departmentNames = loadDepartmentNames(
                result.getContent().stream().map(Account::getDepartmentId).collect(Collectors.toList()));
        List<AccountDTO> list = result.getContent().stream()
                .map(account -> convertToDTO(account, departmentNames))
                .collect(Collectors.toList());

        return new PageResult<>(list, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO login(String username, String password) {
        Account account = accountRepository.findByUsernameAndPasswordAndDeleted(
                username, MD5Util.encode(password), NOT_DELETED)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (account.getStatus() != 1) {
            throw new RuntimeException("账号已禁用");
        }
        return convertToDTO(account);
    }

    /**
     * 把用户输入的关键字拼成 LIKE 用的 "%关键字%"。
     * <p>
     * 关键字里的 \ % _ 对 LIKE 来说是有含义的（分别是转义符和两个通配符），
     * 不转义的话搜一个 "%" 就会把整张表捞出来。这里按 '\' 转义后交给
     * Repository 的 ESCAPE '\' 处理。
     *
     * @return null 表示没有关键字，对应 JPQL 里该筛选条件不生效
     */
    private String buildUsernamePattern(String username) {
        if (username == null) {
            return null;
        }
        String keyword = username.trim();
        if (keyword.isEmpty()) {
            return null;
        }
        String escaped = keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /**
     * 按 id 批量取部门名，供列表场景避免 N+1。
     * 用 findAllById 而不是 findByDeleted：部门是软删除的，历史用户仍要能显示原部门名。
     */
    private Map<Long, String> loadDepartmentNames(Collection<Long> departmentIds) {
        Set<Long> ids = departmentIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return departmentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    /**
     * 部门是可选字段，允许传 null（表示未分配）。传了值就要求部门真实存在，
     * 否则外键会在 flush 时报一个用户看不懂的约束错误。
     * 这里用 findById 而非 findByIdAndDeleted：部门被软删除后，原本挂在它下面的
     * 用户仍然保留引用，编辑这些用户时不应该被"部门不存在"挡住。
     */
    private void validateDepartmentExists(Long departmentId) {
        if (departmentId != null && departmentRepository.findById(departmentId).isEmpty()) {
            throw new RuntimeException("部门不存在");
        }
    }

    private AccountDTO convertToDTO(Account account) {
        return convertToDTO(account, null);
    }

    /**
     * @param departmentNames 已知的 id -> name 映射，用于列表场景避免 N+1；
     *                        传 null 表示按单个 account 的 departmentId 现查。
     */
    private AccountDTO convertToDTO(Account account, Map<Long, String> departmentNames) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setName(account.getName());
        dto.setGender(account.getGender());
        dto.setPhone(account.getPhone());
        dto.setDepartmentId(account.getDepartmentId());
        dto.setDepartmentName(resolveDepartmentName(account.getDepartmentId(), departmentNames));
        dto.setRoleLevel(account.getRoleLevel());
        dto.setStatus(account.getStatus());
        dto.setIsSuper(account.getIsSuper());
        dto.setCreateTime(account.getCreateTime());
        dto.setUpdateTime(account.getUpdateTime());
        return dto;
    }

    private String resolveDepartmentName(Long departmentId, Map<Long, String> departmentNames) {
        if (departmentId == null) {
            return null;
        }
        if (departmentNames != null) {
            return departmentNames.get(departmentId);
        }
        return departmentRepository.findById(departmentId)
                .map(Department::getName)
                .orElse(null);
    }
}
