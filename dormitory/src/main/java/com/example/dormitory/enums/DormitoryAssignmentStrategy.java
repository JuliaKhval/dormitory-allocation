package com.example.dormitory.enums;

public enum DormitoryAssignmentStrategy {
    /** Распределение по порогу среднего балла между двумя общежитиями */
    SCORE_THRESHOLD,
    /** Поочерёдное распределение по всем общежитиям */
    ROUND_ROBIN
}
