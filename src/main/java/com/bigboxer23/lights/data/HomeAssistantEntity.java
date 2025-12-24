package com.bigboxer23.lights.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Map;

/** DTO for Home Assistant entity status */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeAssistantEntity {

	@JsonProperty("entity_id")
	private String entityId;

	private String state;

	private Map<String, Object> attributes;

	@JsonProperty("last_changed")
	private ZonedDateTime lastChanged;

	@JsonProperty("last_reported")
	private ZonedDateTime lastReported;

	@JsonProperty("last_updated")
	private ZonedDateTime lastUpdated;

	private EntityContext context;

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public ZonedDateTime getLastChanged() {
		return lastChanged;
	}

	public void setLastChanged(ZonedDateTime lastChanged) {
		this.lastChanged = lastChanged;
	}

	public ZonedDateTime getLastReported() {
		return lastReported;
	}

	public void setLastReported(ZonedDateTime lastReported) {
		this.lastReported = lastReported;
	}

	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public EntityContext getContext() {
		return context;
	}

	public void setContext(EntityContext context) {
		this.context = context;
	}

	/** Helper method to get an attribute value with type casting */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key, Class<T> type) {
		if (attributes == null) {
			return null;
		}
		Object value = attributes.get(key);
		if (value == null) {
			return null;
		}
		try {
			return (T) value;
		} catch (ClassCastException e) {
			return null;
		}
	}

	/** Helper method to get string attribute */
	public String getAttributeAsString(String key) {
		Object value = attributes != null ? attributes.get(key) : null;
		return value != null ? value.toString() : null;
	}

	/** Helper method to get numeric attribute as Double */
	public Double getAttributeAsDouble(String key) {
		Object value = attributes != null ? attributes.get(key) : null;
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Helper method to get numeric attribute as Integer */
	public Integer getAttributeAsInteger(String key) {
		Object value = attributes != null ? attributes.get(key) : null;
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Helper method to get boolean attribute */
	public Boolean getAttributeAsBoolean(String key) {
		Object value = attributes != null ? attributes.get(key) : null;
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.parseBoolean(value.toString());
	}
}
