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


Install on pi:

Install Jetty

wget -O jetty.zip http://download.eclipse.org/jetty/stable-9/dist/jetty-distribution-9...
sudo -s
mkdir -p /opt/jetty
mv jetty.zip /opt/jetty
cd /opt/jetty
unzip jetty.zip
mv jetty-distribution-9.2.10.v20150310/ runtime
rm jetty.zip
cp runtime/bin/jetty.sh /etc/init.d/jetty
echo JETTY_HOME=`pwd`/runtime > /etc/default/jetty
service jetty start
update-rc.d jetty defaults
mkdir -p /opt/jetty/web/bbq
mkdir -p /opt/jetty/temp
useradd --user-group --shell /bin/false --home-dir /opt/jetty/temp jetty
chown -R jetty:jetty /opt/jetty
echo "JETTY_HOME=/opt/jetty/runtime" > /etc/default/jetty
echo "JETTY_BASE=/opt/jetty/web/bbq" >> /etc/default/jetty
echo "TMPDIR=/opt/jetty/temp" >> /etc/default/jetty


Add -Dvera.url=http://192.168.0.21:3480/ (or whatever the IP of your Vera hub is)
to /opt/web/mybase/start.ini