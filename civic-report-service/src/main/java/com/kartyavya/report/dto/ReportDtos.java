package com.kartyavya.report.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public final class ReportDtos {
    private ReportDtos() {
    }

    /**
     * Multipart request objects must expose JavaBean setters because Spring MVC
     * binds @ModelAttribute values through BeanWrapper property access.
     */
    public static class CreateRequest {
        @NotBlank
        @Size(min = 5, max = 200)
        public String complaintTitle;

        @NotBlank
        @Size(min = 20, max = 3000)
        public String description;

        @NotBlank
        @Size(min = 3, max = 255)
        public String areaLocation;

        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        public Double latitude;

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        public Double longitude;

        @NotNull
        public MultipartFile complaintImage;

        public String getComplaintTitle() {
            return complaintTitle;
        }

        public void setComplaintTitle(String complaintTitle) {
            this.complaintTitle = complaintTitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAreaLocation() {
            return areaLocation;
        }

        public void setAreaLocation(String areaLocation) {
            this.areaLocation = areaLocation;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public MultipartFile getComplaintImage() {
            return complaintImage;
        }

        public void setComplaintImage(MultipartFile complaintImage) {
            this.complaintImage = complaintImage;
        }
    }

    public static class UpdateRequest {
        @NotBlank
        @Size(min = 5, max = 200)
        public String complaintTitle;

        @NotBlank
        @Size(min = 20, max = 3000)
        public String description;

        @NotBlank
        @Size(min = 3, max = 255)
        public String areaLocation;

        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        public Double latitude;

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        public Double longitude;

        public MultipartFile complaintImage;

        public String getComplaintTitle() {
            return complaintTitle;
        }

        public void setComplaintTitle(String complaintTitle) {
            this.complaintTitle = complaintTitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAreaLocation() {
            return areaLocation;
        }

        public void setAreaLocation(String areaLocation) {
            this.areaLocation = areaLocation;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public MultipartFile getComplaintImage() {
            return complaintImage;
        }

        public void setComplaintImage(MultipartFile complaintImage) {
            this.complaintImage = complaintImage;
        }
    }

    public record HistoryResponse(
            String fromStatus,
            String toStatus,
            String remarks,
            Long changedBy,
            String changedByRole,
            Instant changedAt
    ) {
    }

    public record ReportResponse(
            Long complaintId,
            String trackingCode,
            Long citizenId,
            String citizenName,
            String citizenEmail,
            String citizenMobile,
            String complaintTitle,
            String description,
            String areaLocation,
            String complaintImage,
            Double latitude,
            Double longitude,
            String aiCategory,
            String aiSeverity,
            Double aiConfidence,
            boolean aiOverridden,
            String suggestedDepartmentCode,
            Long departmentId,
            String departmentName,
            Long officerId,
            String officerName,
            String officerEmail,
            String officerMobile,
            String status,
            String statusCode,
            String pendingReason,
            String resolutionRemark,
            String resolutionImage,
            Instant complaintDate,
            Instant resolvedDate,
            Long version,
            List<HistoryResponse> statusHistory
    ) {
    }

    public record AssignmentRequest(
            @NotNull @Positive Long departmentId,
            Long officerId,
            @NotBlank @Size(min = 5, max = 1000) String remarks
    ) {
    }

    public static class StatusUpdateRequest {
        @NotBlank
        public String status;

        @Size(max = 2000)
        public String resolutionRemark;

        public MultipartFile resolutionImage;
        public String correctedCategory;
        public String correctedSeverity;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getResolutionRemark() {
            return resolutionRemark;
        }

        public void setResolutionRemark(String resolutionRemark) {
            this.resolutionRemark = resolutionRemark;
        }

        public MultipartFile getResolutionImage() {
            return resolutionImage;
        }

        public void setResolutionImage(MultipartFile resolutionImage) {
            this.resolutionImage = resolutionImage;
        }

        public String getCorrectedCategory() {
            return correctedCategory;
        }

        public void setCorrectedCategory(String correctedCategory) {
            this.correctedCategory = correctedCategory;
        }

        public String getCorrectedSeverity() {
            return correctedSeverity;
        }

        public void setCorrectedSeverity(String correctedSeverity) {
            this.correctedSeverity = correctedSeverity;
        }
    }

    public record NearbyReportResponse(
            Long complaintId,
            String complaintTitle,
            String areaLocation,
            Double latitude,
            Double longitude,
            String aiCategory,
            String aiSeverity,
            String status,
            Instant complaintDate
    ) {
    }

    public record TrackingResponse(
            String trackingCode,
            String complaintTitle,
            String areaLocation,
            String aiCategory,
            String aiSeverity,
            String departmentName,
            String officerName,
            String status,
            String pendingReason,
            Instant complaintDate,
            Instant resolvedDate,
            List<HistoryResponse> statusHistory
    ) {
    }
}
