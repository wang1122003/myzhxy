package com.campus.vo;

import com.campus.entity.Course;
import com.campus.entity.Score;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 成绩视图对象，包含课程信息
 */
@Data
@NoArgsConstructor
public class ScoreVO {

    private Long id;
    private Long studentId;
    private Long courseId;
    private String term;
    private BigDecimal regularScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;
    private BigDecimal totalScore;
    private BigDecimal gpa;
    private Date createTime;
    private Date updateTime;

    // Course Info
    private String courseCode;
    private String courseName;
    private Float credit;

    public ScoreVO(Score score, Course course) {
        if (score != null) {
            BeanUtils.copyProperties(score, this);
            // 使用 score 中的 totalScore，而不是依赖 calculateAndSetTotalScore
            this.totalScore = score.getTotalScore();
        }
        if (course != null) {
            this.courseCode = course.getCourseCode();
            this.courseName = course.getCourseName();
            this.credit = course.getCredit();
        }
    }
} 