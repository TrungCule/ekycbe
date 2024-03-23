package vnpay.com.vn.service.model;

import vnpay.com.vn.domain.Authority;
import vnpay.com.vn.domain.User;

import java.util.Set;

public class UserResp {
    private User user;
    private Set<Authority> authorities;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
}
