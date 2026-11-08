package com.example.perfa6.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 员工新增／编辑请求体。
 *
 * <p>所有字符串字段在反序列化阶段统一去除首尾空白，空白字符串按“未填写”落库为 {@code null}；
 * 因此后续 Bean Validation 校验的是去除空白后的值。</p>
 */
public class EmployeeRequest {

    @NotBlank(message = "工号不能为空")
    @Size(max = 32, message = "工号长度不能超过32个字符")
    private String code;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    @Size(max = 50, message = "部门长度不能超过50个字符")
    private String department;

    @Size(max = 50, message = "职位长度不能超过50个字符")
    private String position;

    @Pattern(regexp = "\\d{11}", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "入职日期格式不正确")
    private String hireDate;

    @Pattern(regexp = "在职|离职", message = "在职状态取值不合法")
    private String status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = normalize(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = normalize(department);
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = normalize(position);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalize(phone);
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = normalize(hireDate);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalize(status);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        int start = 0;
        int end = value.length();
        while (start < end && isBlank(value.charAt(start))) {
            start++;
        }
        while (end > start && isBlank(value.charAt(end - 1))) {
            end--;
        }
        return start == end ? null : value.substring(start, end);
    }

    private static boolean isBlank(char value) {
        return Character.isWhitespace(value) || Character.isSpaceChar(value);
    }
}
