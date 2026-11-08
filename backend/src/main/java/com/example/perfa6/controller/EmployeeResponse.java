package com.example.perfa6.controller;

import com.example.perfa6.domain.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String code,
        String name,
        String department,
        String position,
        String phone,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt) {

    private static final String DEFAULT_STATUS = "在职";

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getCode(),
                employee.getName(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getPhone(),
                employee.getHireDate(),
                employee.getStatus() == null ? DEFAULT_STATUS : employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt());
    }
}
