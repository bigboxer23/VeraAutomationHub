package com.bigboxer23.house;

import com.jones.matt.lights.controllers.vera.VeraController;
import com.jones.matt.lights.controllers.vera.VeraDeviceVO;
import com.jones.matt.lights.controllers.vera.VeraHouseVO;
import com.jones.matt.lights.controllers.vera.VeraRoomVO;
import com.jones.matt.lights.servlets.HubServlet;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 */
public class TestVeraController
{
	private VeraController myController;

	@BeforeClass
	public void setup()
	{
		myController = new VeraController();
		myController.getStatus();
	}

	//Turn off all lights
	//http://192.168.0.21:3480/data_request?id=action&output_format=json&Category=999&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0

	/**
	 * http://192.168.0.21:3480/data_request?id=sdata
	 */
	@Test
	public void testStatus()
	{
		VeraHouseVO aHouse = myController.getStatus();
		assert aHouse != null;
	}

	@Test
	public void testRoomOn() throws InterruptedException
	{
		int aDeviceId = 5;
		if (getRoomStatus(aDeviceId))
		{
			changeRoomState(aDeviceId, false);
			Thread.sleep(2000);
		}
		changeRoomState(aDeviceId, true);
		Thread.sleep(2000);
		assert getRoomStatus(aDeviceId);
	}

	/**
	 * http://192.168.0.21:3480/data_request?id=action&output_format=json&DeviceNum=23&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testDeviceOn() throws InterruptedException
	{
		int aDeviceId = 23;
		if (getDeviceStatus(aDeviceId))
		{
			changeDeviceState(aDeviceId, false);
			Thread.sleep(2000);
		}
		changeDeviceState(aDeviceId, true);
		Thread.sleep(2000);
		assert getDeviceStatus(aDeviceId);
	}

	/**
	 * http://192.168.0.21:3480/data_request?id=action&SceneNum=5&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene
	 */
	@Test
	public void testRunScene()
	{
		myController.doAction(HubServlet.getCommands("Lights/S/Vera/Scene/" + 7 + "/HomeAutomationGateway1&action=RunScene"));
	}

	private void changeDeviceState(int theId, boolean theOn)
	{
		changeState(theId, theOn, "Device");
	}

	private void changeRoomState(int theId, boolean theOn)
	{
		changeState(theId, theOn, "Room");
	}

	private void changeState(int theId, boolean theOn, String theIdentifier)
	{
		myController.doAction(HubServlet.getCommands("Lights/S/Vera/" + theIdentifier + "/" + theId + "/SwitchPower1&action=SetTarget&newTargetValue=" + (theOn ? 1 : 0)));
	}

	private boolean getDeviceStatus(int theId)
	{
		VeraHouseVO aHouse = myController.getStatus();
		for (VeraRoomVO aRoom : aHouse.getRooms())
		{
			for (VeraDeviceVO aDevice : aRoom.getDevices())
			{
				if (aDevice.getId() == theId)
				{
					return aDevice.getStatus();
				}
			}
		}
		return false;
	}

	private boolean getRoomStatus(int theId)
	{
		VeraHouseVO aHouse = myController.getStatus();
		for (VeraRoomVO aRoom : aHouse.getRooms())
		{
			if (aRoom.getId() == theId)
			{
				for (VeraDeviceVO aDevice : aRoom.getDevices())
				{
					if (aDevice.getStatus())
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
