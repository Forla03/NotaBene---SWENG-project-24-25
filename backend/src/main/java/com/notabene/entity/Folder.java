package com.notabene.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "folders",
       uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id","name"}))
public class Folder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="owner_id", nullable=false, updatable=false)
  private Long ownerId;

  @Column(nullable=false, length=120)
  private String name;

  @CreationTimestamp
  @Column(name="created_at", nullable=false, updatable=false)
  private Instant createdAt;

  public Folder() {}
  public Folder(Long ownerId, String name) { this.ownerId = ownerId; this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id){ this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId){ this.ownerId = ownerId; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
}

