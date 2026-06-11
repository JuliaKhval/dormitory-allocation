package com.example.dormitory.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_name", "course", "faculty_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(nullable = false)
    private Integer course;
}