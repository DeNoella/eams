package com.eams.model;

import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "asset_category_id", columnDefinition = "uuid")
    private UUID assetCategoryId;

    @Column(name = "location_id", columnDefinition = "uuid")
    private UUID locationId;

    @Column(name = "can_create")
    @Builder.Default
    private Boolean canCreate = false;

    @Column(name = "can_read")
    @Builder.Default
    private Boolean canRead = false;

    @Column(name = "can_update")
    @Builder.Default
    private Boolean canUpdate = false;

    @Column(name = "can_delete")
    @Builder.Default
    private Boolean canDelete = false;

    @Column(name = "can_approve")
    @Builder.Default
    private Boolean canApprove = false;

    @Column(name = "can_export")
    @Builder.Default
    private Boolean canExport = false;
}