package com.fraud.common.dto.api;

import java.util.Map;

public record ApiErrorDetail(String code, String path, Map<String, String> fieldErrors) {}
