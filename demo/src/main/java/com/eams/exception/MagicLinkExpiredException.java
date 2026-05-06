package com.eams.exception;
public class MagicLinkExpiredException extends RuntimeException {
    public MagicLinkExpiredException(String message) { super(message); }
}