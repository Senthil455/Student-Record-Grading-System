package com.example.studentrecord.dto;

/** Student count for one department, returned by an HQL GROUP BY query. */
public class DepartmentStat {

    private final String department;
    private final Long studentCount;

    public DepartmentStat(String department, Long studentCount) {
        this.department = department;
        this.studentCount = studentCount;
    }

    public String getDepartment() {
        return department;
    }

    public Long getStudentCount() {
        return studentCount;
    }
}
