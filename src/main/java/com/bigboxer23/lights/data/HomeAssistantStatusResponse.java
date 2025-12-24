package com.bigboxer23.lights.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Wrapper DTO for Home Assistant status response containing multiple entities */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeAssistantStatusResponse {

	private List<HomeAssistantEntity> entities;

	public HomeAssistantStatusResponse() {}

	public HomeAssistantStatusResponse(List<HomeAssistantEntity> entities) {
		this.entities = entities;
	}

	public List<HomeAssistantEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<HomeAssistantEntity> entities) {
		this.entities = entities;
	}

	/** Helper method to find an entity by entity_id */
	public HomeAssistantEntity findByEntityId(String entityId) {
		if (entities == null || entityId == null) {
			return null;
		}
		return entities.stream()
				.filter(entity -> entityId.equals(entity.getEntityId()))
				.findFirst()
				.orElse(null);
	}

	/** Helper method to find all entities matching a pattern */
	public List<HomeAssistantEntity> findByEntityIdPattern(String pattern) {
		if (entities == null || pattern == null) {
			return List.of();
		}
		return entities.stream()
				.filter(entity ->
						entity.getEntityId() != null && entity.getEntityId().contains(pattern))
				.toList();
	}

	/**
	 * Helper method to get all entities of a specific domain (e.g., "sensor", "switch", "update")
	 */
	public List<HomeAssistantEntity> findByDomain(String domain) {
		if (entities == null || domain == null) {
			return List.of();
		}
		return entities.stream()
				.filter(entity ->
						entity.getEntityId() != null && entity.getEntityId().startsWith(domain + "."))
				.toList();
	}
}
