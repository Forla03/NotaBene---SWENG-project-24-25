package com.notabene.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tag", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tag_name", columnNames = {"name"})
})
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usando citext in DB; qui lo mappiamo come String.
    @Column(nullable = false, name = "name")
    private String name;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public Long getCreatedBy() {
    return createdBy;
}

public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
}

public OffsetDateTime getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tag)) return false;
    Tag other = (Tag) o;
    return id != null && id.equals(other.id);
}
@Override
public int hashCode() { return 31; }

}
