package com.example.perfa6.controller;

import java.util.List;

public record EmployeePageResponse(List<EmployeeResponse> list, long total, int page, int size) {
}
