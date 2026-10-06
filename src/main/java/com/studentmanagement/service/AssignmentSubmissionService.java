package com.studentmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.storage.blob.models.BlobProperties;
import com.studentmanagement.dto.request.AssignmentSubmissionUploadRequest;
import com.studentmanagement.dto.response.AssignmentSubmissionUploadResponse;
import com.studentmanagement.dto.response.FileAccessResponse;
import com.studentmanagement.dto.response.AssignmentSubmissionsResponse;
import com.studentmanagement.dto.response.AssignmentSubmissionsResponseInner;
import com.studentmanagement.dto.response.SasAccessDetails;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.Teacher;
import com.studentmanagement.entity.User;
import com.studentmanagement.entity.Assignment;
import com.studentmanagement.entity.AssignmentSubmission;
import com.studentmanagement.entity.AssignmentSubmissionStatus;
import com.studentmanagement.entity.FileAsset;
import com.studentmanagement.entity.FileStorageStatus;
import com.studentmanagement.exception.AccessDeniedException;
import com.studentmanagement.exception.ResourceExistException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.AssignmentRepository;
import com.studentmanagement.repository.AssignmentSubmissionRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.FileAssetRepository;
import com.studentmanagement.repository.UserRepository;

@Service
public class AssignmentSubmissionService {
    private final AzureBlobStorageService azureBlobStorageService;
    private final FileAssetRepository fileAssetRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final UserRepository userRepository;

    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    @Value("${azure.storage.container-name}")
    private String containerName;

    public AssignmentSubmissionService(
            FileAssetRepository fileAssetRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            UserRepository userRepository,
            AssignmentRepository assignmentRepository,
            EnrollmentRepository enrollmentRepository,
            AzureBlobStorageService azureBlobStorageService) {
        this.fileAssetRepository = fileAssetRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.azureBlobStorageService = azureBlobStorageService;
    }

    @Transactional
    public AssignmentSubmissionUploadResponse initiateSubmission(Long assignmentId,
            AssignmentSubmissionUploadRequest request, Authentication authentication) {
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));
        Student student = user.getStudent();
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new ResourceNotFoundException("assignment not found " + assignmentId));
        Boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(),
                assignment.getCourse().getId());
        if (!enrolled)
            throw new AccessDeniedException("you haven't enrolled in this course");
        var existingSubmission = assignmentSubmissionRepository.findByStudentIdAndAssignmentId(student.getId(),
                assignment.getId());
        AssignmentSubmission submission;
        if (existingSubmission.isPresent()) {
            submission = existingSubmission.get();
            if (submission.getStatus().equals(AssignmentSubmissionStatus.SUBMITTED)) {
                throw new ResourceExistException("Already submitted");
            }
            FileAsset fileAsset = submission.getFileAsset();
            if (fileAsset != null && !fileAsset.getStatus().equals(FileStorageStatus.DELETED)) {
                throw new ResourceExistException(
                        "An upload already exist for this, please delete that and submit again");
            }

        } else {
            submission = new AssignmentSubmission();

        }
        String blobName = buildBlobName(assignmentId, student.getId(), request.getFileName());
        FileAsset fileAsset = new FileAsset();
        fileAsset.setBlobName(blobName);
        fileAsset.setContainerName(containerName);
        fileAsset.setOriginalFileName(request.getFileName());
        fileAsset.setContentType(request.getContentType());
        fileAsset.setCreatedAt(LocalDateTime.now());
        fileAsset.setFileSize(request.getFileSize());
        fileAsset.setStatus(FileStorageStatus.PENDING);
        FileAsset savedFileAsset = fileAssetRepository.save(fileAsset);
        // AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        // submission.setCreatedAt(null);
        submission.setFileAsset(savedFileAsset);
        submission.setStatus(AssignmentSubmissionStatus.PENDING);
        submission.setStudent(student);
        submission.setCreatedAt(LocalDateTime.now());
        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);
        String uploadUrl = azureBlobStorageService.generateUploadSas(blobName);
        AssignmentSubmissionUploadResponse response = new AssignmentSubmissionUploadResponse();
        response.setBlobName(blobName);
        response.setFileAssetId(savedFileAsset.getId());
        response.setStatus(savedFileAsset.getStatus());
        response.setSubmissionId(savedSubmission.getId());
        response.setUploadUrl(uploadUrl);
        response.setOriginalFileName(savedFileAsset.getOriginalFileName());
        return response;
    }

    @Transactional(readOnly = true)
    public AssignmentSubmissionsResponse getAssignmentSubmissions(
            Long assignmentId,
            AssignmentSubmissionStatus status,
            Pageable pageable,
            Authentication authentication) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(
                () -> new ResourceNotFoundException("assignment not found " + assignmentId));
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("user not found " + authentication.getName()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        Teacher teacher = user.getTeacher();
        boolean ownsCourse = teacher != null
                && assignment.getCourse().getTeacher() != null
                && teacher.getId().equals(assignment.getCourse().getTeacher().getId());

        // TODO: Add @PreAuthorize later. Submission data is accessible only to the
        // owning
        // teacher, the submitting student (self), or an ADMIN. This list endpoint is
        // teacher/admin only.
        if (!isAdmin && !ownsCourse) {
            throw new AccessDeniedException("you are not the teacher of this assignment course");
        }

        Page<AssignmentSubmission> submissionPage = assignmentSubmissionRepository
                .findByAssignmentIdAndStatus(assignmentId, status, pageable);

        List<AssignmentSubmissionsResponseInner> submissions = submissionPage.getContent().stream()
                .map(this::toAssignmentSubmissionResponse)
                .toList();

        AssignmentSubmissionsResponse response = new AssignmentSubmissionsResponse();
        response.setAssignmentId(assignment.getId());
        response.setAssignmentTitle(assignment.getTitle());
        response.setCourseId(assignment.getCourse().getId());
        response.setCourseName(assignment.getCourse().getName());
        response.setMaxMarks(assignment.getMaxMarks());
        response.setTotalEnrolled(enrollmentRepository.countByCourseId(assignment.getCourse().getId()));
        response.setTotalSubmitted(assignmentSubmissionRepository.countByAssignmentIdAndStatus(
                assignmentId, AssignmentSubmissionStatus.SUBMITTED));
        response.setSubmissions(submissions);
        response.setPage((long) submissionPage.getNumber());
        response.setSize((long) submissionPage.getSize());
        response.setTotalPages((long) submissionPage.getTotalPages());
        response.setTotalElements(submissionPage.getTotalElements());
        response.setTotalNumberOfElements((long) submissionPage.getNumberOfElements());
        return response;
    }

    private AssignmentSubmissionsResponseInner toAssignmentSubmissionResponse(AssignmentSubmission submission) {
        AssignmentSubmissionsResponseInner response = new AssignmentSubmissionsResponseInner();
        response.setSubmissionId(submission.getId());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getName());
        response.setStatus(submission.getStatus());
        response.setSubmittedAt(submission.getSubmittedAt());
        FileAsset fileAsset = submission.getFileAsset();
        response.setHasFile(fileAsset != null && fileAsset.getStatus() == FileStorageStatus.UPLOADED);
        response.setFileName(fileAsset == null ? null : fileAsset.getOriginalFileName());
        response.setMarks(submission.getMarks());
        response.setMaxMarks(submission.getAssignment().getMaxMarks());
        response.setFeedback(submission.getFeedback());
        response.setGradedAt(submission.getGradedAt());
        return response;
    }

    public String buildBlobName(Long assignmentId, Long studentId, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return "assignmentSubmissions/assignment-" + assignmentId + "/student-" + studentId + "/" + uuid + "-"
                + fileName;
    }

    @Transactional
    public String completeAssignmentSubmission(Long submissionId, Authentication authentication) {
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("username not found " + authentication.getName()));
        Student student = user.getStudent();
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId).orElseThrow(
                () -> new ResourceNotFoundException("submission not found with this id"));
        if (!submission.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("you can't submit this, who are you");
        }
        if (submission.getStatus().equals(AssignmentSubmissionStatus.SUBMITTED)) {
            return "Already Submitted";
        }
        FileAsset fileAsset = submission.getFileAsset();
        BlobProperties properties = azureBlobStorageService.getBlobProperies(fileAsset.getBlobName());
        if (!azureBlobStorageService.blobExists(fileAsset.getBlobName())
                || (fileAsset.getFileSize() != null && properties.getBlobSize() != fileAsset.getFileSize())) {
            throw new IllegalArgumentException("file not uploaded into azure");
        }
        fileAsset.setStatus(FileStorageStatus.UPLOADED);
        fileAssetRepository.save(fileAsset);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmissionStatus.SUBMITTED);
        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);
        return savedSubmission.getStatus().toString();

    }

    @Transactional(readOnly = true)
    public FileAccessResponse getAssignmentSubmission(
            Long submissionId,
            Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "username not found " + username));

        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "submission not found " + submissionId));

        var course = submission.getAssignment().getCourse();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        Teacher teacher = user.getTeacher();
        boolean ownsCourse = teacher != null
                && course.getTeacher() != null
                && teacher.getId().equals(course.getTeacher().getId());
        Student student = user.getStudent();
        boolean isOwner = student != null
                && student.getId().equals(submission.getStudent().getId());

        if (!isAdmin && !ownsCourse && !isOwner) {
            throw new AccessDeniedException("you can't access this");
        }
        if (isOwner && !enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new AccessDeniedException("you haven't enrolled in this course");
        }
        FileAsset fileAsset = submission.getFileAsset();
        if (fileAsset == null || !fileAsset.getStatus().equals(FileStorageStatus.UPLOADED)) {
            throw new ResourceNotFoundException(
                    "you haven't submitted the assignment yet please do it you lazy pellow");
        }
        SasAccessDetails sasAccessDetails = azureBlobStorageService.generateReadSas(fileAsset.getBlobName());
        FileAccessResponse response = new FileAccessResponse();
        response.setAccessFile(sasAccessDetails.getAccessUrl());
        response.setContentType(fileAsset.getContentType());
        response.setFileAccessId(fileAsset.getId());
        response.setFileName(fileAsset.getOriginalFileName());
        response.setFileSize(fileAsset.getFileSize());
        // response.setFileAccessId(fileAsset.getId());
        response.setExpiresAt(sasAccessDetails.getExpiry());
        return response;

    }

    @Transactional
    public String deleteSubmittedFile(Long submissionId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found"));
        Student student = user.getStudent();
        if (student == null) {
            throw new ResourceNotFoundException("Student not found");
        }
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId).orElseThrow(
                () -> new ResourceNotFoundException("submission not found"));
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(),
                submission.getAssignment().getCourse().getId())) {
            throw new ResourceNotFoundException("you are not enrolled in this course");
        }
        if (!student.getId().equals(submission.getStudent().getId())) {
            throw new AccessDeniedException("you can't do it ");
        }
        FileAsset fileAsset = submission.getFileAsset();
        if (fileAsset == null) {
            throw new ResourceNotFoundException("uploaded file not found");
        }
        if (fileAsset.getStatus() == FileStorageStatus.DELETED) {
            return "file already deleted";
        }
        azureBlobStorageService.delete(fileAsset.getBlobName());
        fileAsset.setStatus(FileStorageStatus.DELETED);
        fileAssetRepository.save(fileAsset);
        submission.setStatus(AssignmentSubmissionStatus.PENDING);
        submission.setSubmittedAt(null);
        assignmentSubmissionRepository.save(submission);
        return "Submission file deleted successfully";

    }
}
