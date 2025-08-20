package com.notabene.service.support;

public interface CurrentUserResolver {
    Long currentUserId();
    String principal();
}