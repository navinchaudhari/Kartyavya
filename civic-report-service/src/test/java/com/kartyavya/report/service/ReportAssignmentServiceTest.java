package com.kartyavya.report.service;

import com.kartyavya.contracts.AccessContracts;
import com.kartyavya.contracts.ReportCategory;
import com.kartyavya.contracts.ReportStatus;
import com.kartyavya.contracts.Severity;
import com.kartyavya.report.dto.ReportDtos;
import com.kartyavya.report.entity.Report;
import com.kartyavya.report.integration.AccessClient;
import com.kartyavya.report.integration.AiClient;
import com.kartyavya.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReportAssignmentServiceTest {
    private ReportRepository reports;
    private AccessClient access;
    private OutboxService outbox;
    private ReportService service;
    private Authentication admin;
    private Report report;

    @BeforeEach
    void setUp() {
        reports = mock(ReportRepository.class);
        access = mock(AccessClient.class);
        outbox = mock(OutboxService.class);
        service = new ReportService(
                reports,
                access,
                mock(AiClient.class),
                mock(FileStorageService.class),
                mock(LocationService.class),
                outbox,
                new ReportMapper()
        );
        admin = mock(Authentication.class);
        when(admin.getName()).thenReturn("1");

        report = new Report();
        report.setId(50L);
        report.setTrackingCode("KTY-TEST000001");
        report.setCitizenId(9L);
        report.setCitizenName("Citizen One");
        report.setCitizenEmail("citizen@example.com");
        report.setCitizenMobile("9876543210");
        report.setTitle("Water leakage near school");
        report.setDescription("A major water leakage is blocking the public road near the school.");
        report.setAreaLocation("School Road");
        report.setImagePath("/uploads/reports/test.jpg");
        report.setLatitude(21.05);
        report.setLongitude(75.77);
        report.setAiCategory(ReportCategory.WATER_LEAKAGE);
        report.setAiSeverity(Severity.HIGH);
        report.setAiConfidence(92.0);
        report.setStatus(ReportStatus.PENDING_OFFICER_ASSIGNMENT);
        when(reports.findById(50L)).thenReturn(Optional.of(report));
        when(reports.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void keepsDepartmentOwnershipWhenOfficerIsStillUnavailable() {
        when(access.department(4L)).thenReturn(new AccessContracts.DepartmentInfo(4L, "Water & Drainage", "water@example.com", true));

        var result = service.assign(50L,
                new ReportDtos.AssignmentRequest(4L, null, "Department created; officer allocation is pending"),
                admin, "correlation-1");

        assertThat(result.statusCode()).isEqualTo("PENDING_OFFICER_ASSIGNMENT");
        assertThat(result.departmentId()).isEqualTo(4L);
        assertThat(result.officerId()).isNull();
        verify(outbox).enqueue(eq("report.officer.pending"), eq("correlation-1"), any());
    }

    @Test
    void rejectsOfficerAllocatedToAnotherDepartment() {
        when(access.department(4L)).thenReturn(new AccessContracts.DepartmentInfo(4L, "Water & Drainage", "water@example.com", true));
        when(access.officer(20L)).thenReturn(new AccessContracts.OfficerInfo(20L, "Road Officer", "road@example.com", "9876543210", 1L, "Roads", true));

        assertThatThrownBy(() -> service.assign(50L,
                new ReportDtos.AssignmentRequest(4L, 20L, "Attempted cross-department assignment"),
                admin, "correlation-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");

        verify(reports, never()).save(argThat(saved -> saved.getOfficerId() != null));
    }
}
