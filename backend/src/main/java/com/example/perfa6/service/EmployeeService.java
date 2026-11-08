package com.example.perfa6.service;

import com.example.perfa6.controller.EmployeeRequest;
import com.example.perfa6.domain.Employee;
import com.example.perfa6.exception.BizException;
import com.example.perfa6.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Service
public class EmployeeService {

    public static final int MAX_PAGE_SIZE = 100;
    public static final String STATUS_ACTIVE = "在职";
    public static final String STATUS_RESIGNED = "离职";

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Employee> list(String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        String condition = keyword == null ? "" : keyword.trim();
        if (condition.isEmpty()) {
            return employeeRepository.findAll(pageable);
        }
        return employeeRepository.searchByKeyword(condition, pageable);
    }

    @Transactional(readOnly = true)
    public Employee get(long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BizException(40401, "员工不存在", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Employee create(EmployeeRequest request) {
        String code = request.getCode();
        if (employeeRepository.existsByCodeIgnoreCase(code)) {
            throw new BizException(400, "工号已存在");
        }
        Employee employee = new Employee();
        applyEditableFields(employee, request);
        LocalDateTime now = now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BizException(40401, "员工不存在", HttpStatus.NOT_FOUND));
        String code = request.getCode();
        if (employeeRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BizException(400, "工号已存在");
        }
        applyEditableFields(employee, request);
        LocalDateTime now = now();
        if (!now.isAfter(employee.getUpdatedAt())) {
            now = employee.getUpdatedAt().plusSeconds(1);
        }
        employee.setUpdatedAt(now);
        return employeeRepository.save(employee);
    }

    private void applyEditableFields(Employee employee, EmployeeRequest request) {
        employee.setCode(request.getCode());
        employee.setName(request.getName());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setPhone(request.getPhone());
        employee.setHireDate(parseHireDate(request.getHireDate()));
        employee.setStatus(normalizeStatus(request.getStatus()));
    }

    private LocalDate parseHireDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BizException(400, "入职日期格式不正确");
        }
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return STATUS_ACTIVE;
        }
        if (!STATUS_ACTIVE.equals(value) && !STATUS_RESIGNED.equals(value)) {
            throw new BizException(400, "在职状态取值不合法");
        }
        return value;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZONE).truncatedTo(ChronoUnit.SECONDS);
    }
}
