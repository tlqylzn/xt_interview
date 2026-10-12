package com.xt.interview.service;

import com.xt.interview.dto.AccountDTO;
import com.xt.interview.dto.PageResult;

import java.util.List;

public interface AccountService {

    AccountDTO createAccount(AccountDTO dto);

    AccountDTO updateAccount(Long id, AccountDTO dto);

    void deleteAccount(Long id);

    AccountDTO getAccountById(Long id);

    List<AccountDTO> getAllAccounts();

    /**
     * 用户列表的搜索 + 分页。
     *
     * @param username  用户名模糊匹配关键字，为空表示不筛选
     * @param status    状态筛选，null 表示不筛选
     * @param roleLevel 权限级别筛选，null 表示不筛选
     * @param page      页码，从 0 开始
     * @param size      每页条数
     */
    PageResult<AccountDTO> searchAccounts(String username, Integer status, Integer roleLevel, int page, int size);

    AccountDTO login(String username, String password);
}
