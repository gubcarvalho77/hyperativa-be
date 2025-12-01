package com.hyperativa.be.dtos;

import org.springframework.web.multipart.MultipartFile;

public record CreditCardUploadRequest(
        MultipartFile file
) {}
