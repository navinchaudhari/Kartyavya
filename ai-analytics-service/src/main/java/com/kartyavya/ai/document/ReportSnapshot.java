package com.kartyavya.ai.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("report_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class ReportSnapshot {
	@Id
	private String id;
	@Indexed(unique = true)
	private Long reportId;
	private String trackingCode;
	private Long citizenId;
	private String title;
	private String areaLocation;
	private Double latitude;
	private Double longitude;
	private String category;
	private String severity;
	private Double confidence;
	private Long departmentId;
	private String departmentName;
	private Long officerId;
	private String officerName;
	private String status;
	private boolean aiOverridden;
	private Instant createdAt;
	private Instant resolvedAt;
	private Instant updatedAt = Instant.now();
}
