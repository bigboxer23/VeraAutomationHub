package com.bigboxer23.lights.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** DTO for Home Assistant entity context */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityContext {

	private String id;

	@JsonProperty("parent_id")
	private String parentId;

	@JsonProperty("user_id")
	private String userId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
