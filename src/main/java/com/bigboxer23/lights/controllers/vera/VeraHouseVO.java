package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.openHAB.OpenHABHouse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Data structure returned from vera when status is requested
 */
@Data
@Schema(description = "JSON object representing devices, rooms, scenes within the house")
public class VeraHouseVO
{
	@Schema(description = "Rooms within the house")
	private List<VeraRoomVO> rooms;

	@Schema(description = "if the front door camera is paused, for how long (sec)")
	private int frontDoorPauseTime;

	public VeraHouseVO(OpenHABHouse theHouse)
	{
		rooms = VeraRoomVO.fromOpenHab(theHouse);
	}
}
