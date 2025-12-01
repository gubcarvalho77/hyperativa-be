package com.hyperativa.be.services.creditcard.handlers;

@FunctionalInterface
public interface UploadedFileHandler<T, R> {
    R apply(T input);
}
