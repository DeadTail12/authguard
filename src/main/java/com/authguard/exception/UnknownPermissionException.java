package com.authguard.exception;

import java.util.Collection;

public class UnknownPermissionException extends RuntimeException {
    public UnknownPermissionException(Collection<String> unknownNames) {
        super("Unknown permission(s): " + unknownNames);
    }
}
