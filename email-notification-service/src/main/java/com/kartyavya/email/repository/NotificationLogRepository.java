package com.kartyavya.email.repository;

import com.kartyavya.email.document.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
	List<NotificationLog> findTop200ByOrderByCreatedAtDesc();
}
