package com.auth.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RolePermissionId implements Serializable {

    private String roleId;
    private String permissionId;
}