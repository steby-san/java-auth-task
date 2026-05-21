package com.auth.model;

import com.auth.model.Role;
import com.auth.model.User;
import com.auth.model.UserRoleId;
import jakarta.persistence.*;

@Entity
@Table(name = "user_role_mappings")
public class UserRoleMapping {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;
}