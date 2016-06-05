# Step-by-step debugger for Apache JMeter 

![logo](/src/main/resources/com/blazemeter/jmeter/debugger/logo.png) 

Implemented by [BlazeMeter.com](http://blazemeter.com/) 

## Features
 - step over single component
 - breakpoints and running until paused
 - current values for variables and properties shown
 - evaluate expressions pane 
 - resolved execution tree displayed
 - current element and current sampler scope highlighted in the tree
 - element UI shown with resolved variables and functions

## Installation

Install it through [JMeter Plugins Manager](http://jmeter-plugins.org/wiki/PluginsManager/). Or checkout the source and build with `mvn clean package`, then take JAR from `target` directory.

## Usage

 - Find it under "Run" item of main menu. 
 - Choose the thread group to debug from combo-box. 
 - Press "Start" to start debugging, "Stop" to abort it. 
 - Use "Step Over" or "Continue/Pause" to perform debugging.
 - Right-click on the element in tree allows to set breakpoint (if appliable to the element).
 - Type expression on "Evaluate" tab and execute it, you can even set variables and properties by using appropriate JMeter functions.

## Known Limitations 
 - Only one thread group at a time can be debugged (evaluate panel allows to mitigate the limitation)
 - Module Controllers and Include Controllers are not supported

## Roadmap
 - double run erases names
 - original jmx modified
  
 - choose license
 - Maximize on Windows / Non modal window 
 - expand sampler path in tree to reveal it
 - logic to stop on Sampler bp
 - logic to stop on Listener bp only once after sample 
 - choose TG from context of tree and show debugger started?
 
 - remove controls from DBG TG UI (action on error)
 - italic font in tree not shown on Mac
 - shortcut to toggle BP
 - shortcut to invoke debugger
 - add icon on toolbar?
 - revive Stop button
 - revive variable change highlight
 - fix disabled items appearing in tree
 
 
