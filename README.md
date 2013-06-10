Selendroid
==========

[![Build Status](https://api.travis-ci.org/DominikDary/selendroid.png)](https://travis-ci.org/DominikDary/selendroid)

Selendroid is a test automation framework which drives of the UI of Android native and hybrid applications (apps). Tests are written using the Selenium 2 client API and for testing the application under test must not be modified. 

Selendroid can be used on emulators and real devices and can be integrated as a node into the Selenium Grid for scaling and parallel testing. 


You want more details?
----------------------

Check out our [documentation](http://dominikdary.github.io/selendroid/).

Windows7
--------
$JAVA_HOME = C:\Program Files\Java\jdk1.7.0_21

Program Files <= ShellCommand Exec Error

~/src/main/java/io/selendroid/android/JavaSdk.java
  - return adbCommand.toString();
  + return "\"" + adbCommand.toString() + "\"";

