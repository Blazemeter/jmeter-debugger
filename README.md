# Step-by-step debugger for Apache JMeter 

![logo](/src/main/resources/com/blazemeter/jmeter/debugger/logo.png) 

Implemented by [BlazeMeter.com](http://blazemeter.com/), released under Apache 2.0 License 

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

 - Find it under "Run" item of main menu. Consider saving your JMX before starting debugger.
 - Choose the thread group to debug from combo-box. 
 - Press "Start" to start debugging, "Stop" to abort it. 
 - Use "Step Over" or "Continue/Pause" to perform debugging.
 - Right-click on the element in tree allows to set breakpoint (if appliable to the element).
 - Type expression on "Evaluate" tab and execute it, you can even set variables and properties by using appropriate JMeter functions.
 
## Known Limitations 
 - Only one thread group at a time can be debugged (evaluate panel allows to mitigate the limitation)
 - Module Controllers and Include Controllers are not supported

## Support

The best place to report issues and discuss the debugger is jmeter-plugins [support forums](https://groups.google.com/forum/#!forum/jmeter-plugins/).

## Roadmap
 - choose license

 - fix freeze after continue + pause
 - choose TG from context of tree and show debugger started?
 - Maximize on Windows / Non modal window (what to do with all the collision cases) 
 - expand sampler path in tree to reveal it
 - logic to stop on Sampler bp
 - logic to stop on Listener bp only once after sample 
 
 - remove controls from DBG TG UI (action on error)
 - italic font in tree not shown on Mac
 - shortcut to toggle BP
 - shortcut to invoke debugger
 - add icon on toolbar?
 - revive Stop button
 - revive variable change highlight
 - fix disabled items appearing in tree
 
 
