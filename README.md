AutomationHub
=============

  This project is a java web archive deployable which contains servlets to control different home automation technologies.
There's also a scheduler task running to automatically run specific actions based on time, weather, or sunrise/sunset.

  The project consists of a few core components:
  
  Servlet to control specific technologies.  Currently it supports three "raw" controllers: X-10 (/X/*), Philips Hue (/H/*), and
another project to control a garage opener (and retrieve temperature), https://github.com/bigboxer23/PiGarage (/G/*).  There are
also aggregating controllers to control specific rooms or whole house which wrap the raw controllers into a single entity.
(/Kitchen/*, /BathRoom/*, /LivingRoom/*, /AllLights/*).  Finally there are a couple special controllers with specific 
functionality.  The WeatherController (/Weather/*)  will set a Hue light to a specific color based on the outdoor temperature.
DaylightController (/Daylight/*) will return true/false based on if its daylight currently.
  
  The scheduler portion of the app runs a timer task once a minute and allows scheduling tasks to run based on time or events
occurring (like sunset).
  
  There's a web-app included to allow setting up new tasks to run automatically (TODO)
  
  Another web-app for manual control of lights and garage door.  It displays buttons for available light controls, a button with
status to close/open a garage door, and outdoor temperature.  This page is accessible at /HouseLights.html.
