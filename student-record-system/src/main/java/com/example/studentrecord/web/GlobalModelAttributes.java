package com.example.studentrecord.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Values shared by every Thymeleaf page (used by the sidebar, topbar and footer). */
@ControllerAdvice(basePackages = "com.example.studentrecord.web")
public class GlobalModelAttributes {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @ModelAttribute("currentYear")
    public int currentYear() {
        return Year.now().getValue();
    }

    @ModelAttribute("academicYear")
    public String academicYear() {
        int year = Year.now().getValue();
        int start = LocalDate.now().getMonthValue() >= 6 ? year : year - 1;
        return start + "\u2013" + (start + 1);
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now();
    }

    @ModelAttribute("todayLabel")
    public String todayLabel() {
        return LocalDate.now().format(DATE_FORMAT);
    }
}
