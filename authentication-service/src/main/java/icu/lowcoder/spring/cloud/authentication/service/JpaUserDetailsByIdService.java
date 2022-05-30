package icu.lowcoder.spring.cloud.authentication.service;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.commons.wechat.model.UserInfo;
import icu.lowcoder.spring.commons.wechat.model.WebUserInfo;
import icu.lowcoder.spring.cloud.authentication.Constants;
import icu.lowcoder.spring.cloud.authentication.EmptyPasswordEncoder;
import icu.lowcoder.spring.cloud.authentication.dao.AccountRepository;
import icu.lowcoder.spring.cloud.authentication.dict.WeChatAppType;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import icu.lowcoder.spring.cloud.authentication.entity.WeChatAppBinding;
import icu.lowcoder.spring.cloud.authentication.exception.UserAlreadyExistsException;
import icu.lowcoder.spring.cloud.authentication.exception.UserRegistrationException;
import icu.lowcoder.spring.cloud.authentication.oauth2.provider.wechat.WeChatUserAccountModel;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JpaUserDetailsByIdService implements UserDetailsService, UserRegistrationService, UserDetailsByIdService {

    private final AccountRepository accountRepository;

    private static final EmptyPasswordEncoder EMPTY_PASSWORD_ENCODER = new EmptyPasswordEncoder();

    public JpaUserDetailsByIdService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private String defaultRolePrefix = "ROLE_";

    @Override
    public AccountModel loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("未绑定手机号码");
        }

        Account account = accountRepository.findByPhone(username);
        if (account == null) {
            throw new UsernameNotFoundException("手机号未注册: " + username);
        }

        AccountModel accountModel = new AccountModel();
        BeanUtils.copyProperties(account, accountModel, "authorities");

        if (StringUtils.hasText(account.getAuthorities())) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String authority : account.getAuthorities().split(",")) {
                authorities.add(new SimpleGrantedAuthority(authority.startsWith(defaultRolePrefix) ? authority.toUpperCase() : authority.toLowerCase()));
            }

            accountModel.setAuthorities(authorities);
        }

        return accountModel;
    }

    @Transactional
    @Override
    public AccountModel addUser(UserDetails user) throws UserAlreadyExistsException {
        if (user == null) {
            throw new UserRegistrationException("UserDetails must be not null");
        }

        if(StringUtils.hasText(user.getUsername())) {
            if (accountRepository.existsByPhone(user.getUsername())) {
                throw new UserAlreadyExistsException("手机号已注册：" + user.getUsername());
            }

            if (!user.getUsername().matches("\\d{11}")) {
                throw new UserRegistrationException("手机号格式不正确：" + user.getUsername());
            }
        }

        Account account = new Account();
        account.setEnabled(true);
        account.setPhone(user.getUsername());
        account.setRegisterTime(new Date());
        account.setPassword(user.getPassword());

        if (!StringUtils.hasText(user.getPassword())) {
            account.setPassword(Constants.EMPTY_ENCODED_PASSWORD);
        }
        accountRepository.save(account);

        AccountModel accountModel = new AccountModel();
        BeanUtils.copyProperties(account, accountModel);

        return accountModel;
    }

    @Override
    public boolean alreadyExistPhone(String phone) {
        return accountRepository.existsByPhone(phone);
    }

    @Override
    @Transactional
    public WeChatUserAccountModel addWeChatUser(UserInfo weChatUser) {
        if (weChatUser == null) {
            throw new UserRegistrationException("WeChatUser must be not null");
        }

        Account account = new Account();
        account.setEnabled(true);
        account.setName(weChatUser.getNickName());
        account.setRegisterTime(new Date());
        account.setPassword(Constants.EMPTY_ENCODED_PASSWORD);

        WeChatAppBinding binding = new WeChatAppBinding();
        binding.setAccount(account);
        binding.setAppId(weChatUser.getAppId());
        binding.setAppType(WeChatAppType.MINI_PROGRAM);
        binding.setOpenId(weChatUser.getOpenId());
        binding.setUnionId(weChatUser.getUnionId());
        account.getWeChatAppBindings().add(binding);

        accountRepository.save(account);

        WeChatUserAccountModel accountModel = new WeChatUserAccountModel();
        BeanUtils.copyProperties(account, accountModel);
        accountModel.setOpenId(weChatUser.getOpenId());

        return accountModel;
    }

    @Override
    @Transactional
    public void bindWeChatUser(UUID accountId, UserInfo weChatUser) {
        if (weChatUser == null) {
            throw new UserRegistrationException("WeChatUser must be not null");
        }

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("账户不存在"));
        // 如果不存在openId绑定，则新增
        if (account.getWeChatAppBindings().stream().noneMatch(ab -> ab.getOpenId().equals(weChatUser.getOpenId()))) {
            WeChatAppBinding binding = new WeChatAppBinding();

            binding.setAccount(account);
            binding.setAppId(weChatUser.getAppId());
            binding.setAppType(WeChatAppType.MINI_PROGRAM);
            binding.setOpenId(weChatUser.getOpenId());
            binding.setUnionId(weChatUser.getUnionId());
            account.getWeChatAppBindings().add(binding);

            accountRepository.save(account);
        }
    }

    @Override
    public WeChatUserAccountModel addWeChatWebUser(String appId, WebUserInfo webUserInfo) {
        if (webUserInfo == null) {
            throw new UserRegistrationException("WebUserInfo must be not null");
        }

        Account account = new Account();
        account.setEnabled(true);
        account.setName(webUserInfo.getNickname());
        account.setRegisterTime(new Date());
        account.setPassword(Constants.EMPTY_ENCODED_PASSWORD);

        WeChatAppBinding binding = new WeChatAppBinding();
        binding.setAccount(account);
        binding.setAppId(appId);
        binding.setAppType(WeChatAppType.WEB_APP);
        binding.setOpenId(webUserInfo.getOpenid());
        binding.setUnionId(webUserInfo.getUnionid());
        account.getWeChatAppBindings().add(binding);

        accountRepository.save(account);

        WeChatUserAccountModel accountModel = new WeChatUserAccountModel();
        BeanUtils.copyProperties(account, accountModel);
        accountModel.setOpenId(webUserInfo.getOpenid());

        return accountModel;
    }

    @Override
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        if (id == null) {
            throw new UsernameNotFoundException("账户id不能为空");
        }

        Account account = accountRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("账户不存在:" + id));

        AccountModel accountModel = new AccountModel();
        BeanUtils.copyProperties(account, accountModel, "authorities");

        if (StringUtils.hasText(account.getAuthorities())) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String authority : account.getAuthorities().split(",")) {
                authorities.add(new SimpleGrantedAuthority(authority.startsWith(defaultRolePrefix) ? authority.toUpperCase() : authority.toLowerCase()));
            }

            accountModel.setAuthorities(authorities);
        }

        return accountModel;
    }
}
