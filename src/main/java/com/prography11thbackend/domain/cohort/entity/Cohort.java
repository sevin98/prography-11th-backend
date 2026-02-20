package com.prography11thbackend.domain.cohort.entity;

import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cohorts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cohort extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer number; // 기수 번호 (10, 11)

    @OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @Builder
    public Cohort(Integer number) {
        this.number = number;
    }

    public void addTeam(Team team) {
        this.teams.add(team);
        team.setCohort(this);
    }
}
