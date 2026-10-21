package com.flowcore.workflow.auth.api.dto;

import java.util.UUID;

public record AccountSummary(UUID id, String displayName, String login) {
}
