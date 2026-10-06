package com.studentmanagement.dto.response;

import java.util.List;

public class AssignmentSubmissionsResponse {
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private Integer maxMarks;
    private Long totalEnrolled;
    private Long totalSubmitted;
    private List<AssignmentSubmissionsResponseInner> submissions;
    private Long page;
    private Long size;
    private Long totalPages;
    private Long totalElements;
    private Long totalNumberOfElements;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public Long getTotalEnrolled() { return totalEnrolled; }
    public void setTotalEnrolled(Long totalEnrolled) { this.totalEnrolled = totalEnrolled; }
    public Long getTotalSubmitted() { return totalSubmitted; }
    public void setTotalSubmitted(Long totalSubmitted) { this.totalSubmitted = totalSubmitted; }
    public List<AssignmentSubmissionsResponseInner> getSubmissions() { return submissions; }
    public void setSubmissions(List<AssignmentSubmissionsResponseInner> submissions) { this.submissions = submissions; }
    public Long getPage() { return page; }
    public void setPage(Long page) { this.page = page; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Long getTotalPages() { return totalPages; }
    public void setTotalPages(Long totalPages) { this.totalPages = totalPages; }
    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
    public Long getTotalNumberOfElements() { return totalNumberOfElements; }
    public void setTotalNumberOfElements(Long totalNumberOfElements) { this.totalNumberOfElements = totalNumberOfElements; }
}
