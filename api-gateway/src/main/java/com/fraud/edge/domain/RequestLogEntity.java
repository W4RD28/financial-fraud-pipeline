package com.fraud.edge.domain;

import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("request_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestLogEntity implements Persistable<String> {

    @Id private String requestId;

    private String method;
    private String path;
    private String queryParams;
    private String clientIp;
    private String userAgent;
    private String targetService;
    private Integer statusCode;
    private long durationMs;
    private Instant requestedAt;

    @Transient @Builder.Default private boolean isNew = true;

    @Override
    public String getId() {
        return requestId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
