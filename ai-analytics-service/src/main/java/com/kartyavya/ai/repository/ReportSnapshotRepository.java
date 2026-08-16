package com.kartyavya.ai.repository;

import com.kartyavya.ai.document.ReportSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface ReportSnapshotRepository extends MongoRepository<ReportSnapshot, String> {
	Optional<ReportSnapshot> findByReportId(Long id);
}
