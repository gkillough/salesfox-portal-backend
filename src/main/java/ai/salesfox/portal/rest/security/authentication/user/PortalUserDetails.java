package ai.salesfox.portal.rest.security.authentication.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PortalUserDetails implements UserDetails {
    private List<String> roles;
    private String username;
    private String password;
    private boolean isAccountLocked;
    private boolean isActive;

    public PortalUserDetails(List<String> roles, String username, String password, boolean isAccountLocked, boolean isActive) {
        this.roles = roles;
        this.username = username;
        this.password = password;
        this.isAccountLocked = isAccountLocked;
        this.isActive = isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
