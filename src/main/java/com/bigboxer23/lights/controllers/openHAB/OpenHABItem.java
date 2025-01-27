package com.bigboxer23.lights.controllers.openHAB;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Optional;

/** An OpenHAB device (item) */
public class OpenHABItem {
	@SerializedName("name")
	private String myName;

	@SerializedName("label")
	private String myLabel;

	@SerializedName("state")
	private String myState;

	@SerializedName("type")
	private String myType;

	@SerializedName("category")
	private String myCategory;

	@SerializedName("tags")
	private List<String> myTags;

	@SerializedName("members")
	private List<OpenHABItem> myItems;

	public List<OpenHABItem> getItems() {
		return myItems;
	}

	public String getName() {
		return myName;
	}

	public String getState() {
		return getLevel().equalsIgnoreCase("0") ? "0" : "1";
	}

	public String getLevel() {
		if (myType.equalsIgnoreCase("color")) {
			if (myState.lastIndexOf(",") < 0) {
				return "0";
			}
			return myState.substring(myState.lastIndexOf(",") + 1);
		} else if (myType.equalsIgnoreCase("switch")) {
			return myState.equalsIgnoreCase("off") || myState.equalsIgnoreCase("NULL") ? "0" : "1";
		}
		if (myType.equalsIgnoreCase("group")
				&& myName.toLowerCase().contains("motion")
				&& myItems.stream().anyMatch(item -> getItemIdentifier(item).equalsIgnoreCase(myLabel + " sensor"))) {
			return myItems.stream()
					.filter(item -> getItemIdentifier(item).equalsIgnoreCase(myLabel + " sensor"))
					.findAny()
					.map(OpenHABItem::getState)
					.orElse(myState);
		}
		return myState;
	}

	private String getItemIdentifier(OpenHABItem item) {
		return Optional.ofNullable(item.getLabel()).orElse(item.getName());
	}

	public int getIntLevel() {
		String aLevel = getLevel();
		try {
			return Integer.parseInt(aLevel);
		} catch (NumberFormatException aNFE) {
			return 0;
		}
	}

	public String getType() {
		if (getTags().contains("ignore")) {
			return "0";
		}
		switch (myType.toLowerCase()) {
			case "dimmer":
			case "color":
				return "2";
			case "switch":
				return "3";
		}
		return "0";
	}

	public String getCategory() {
		return myCategory;
	}

	public String getLabel() {
		return myLabel;
	}

	public List<String> getTags() {
		return myTags;
	}
}
