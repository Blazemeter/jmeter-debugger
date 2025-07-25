# Step-by-step debugger for Apache JMeter 
Implemented by

<picture>
 <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Blazemeter/jmeter-bzm-commons/refs/heads/master/src/main/resources/dark-theme/blazemeter-by-perforce-logo.png">
 <img src="https://raw.githubusercontent.com/Blazemeter/jmeter-bzm-commons/refs/heads/master/src/main/resources/light-theme/blazemeter-by-perforce-logo.png">
</picture>

released under Apache 2.0 License 

## Features
 - step over single component
 - breakpoints and running until paused
 - current values for variables and properties shown
 - evaluate expressions pane 
 - resolved execution tree displayed
 - current element and current sampler scope highlighted in the tree
 - element UI shown with resolved variables and functions

## Installation

Install it through [JMeter Plugins Manager](http://jmeter-plugins.org/wiki/PluginsManager/). Or checkout the source and build with `mvn clean package`, then take the JAR from `target` directory.

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

# Changelog

__v0.6__ (upcoming)
 - fix variables in assertions and controllers  

__v0.5__ 22 sep 2017
 - add last sampler result tab
 - choose TG from context of tree
 - fix modification thread groups   
 - fix italic font in tree not shown on Mac
 - fix blue color for current element is blue-on-blue for Mac
 - fix logging lab for old jmeter versions
 - fix clear data in info tabs and listeners

__v0.4__ 24 jul 2017
 - migrate logging to SLF4J
 - fix variables in TestElement.name
 - fix variable in propMap
 - fix disable original components
 - fix changing tree model in debbugger after test started
 - fix changing original tree model after closing debbugger

__v0.3__, 21 jun 2016
 - handle Transaction Controller
 - expand sampler path in tree to reveal it
 - logic to stop on first Sampler Scope element for Sampler Breakpoint

__v0.2__, 13 jun 2016
 - disable variables/properties/evaluate update on continue, to prevent deadlock
 - fix breakpoint functioning
 - remove controls from DBG TG UI (action on error)
 - add icon on toolbar (keyboard shortcut is not possible, sorry)

__v0.1__, 7 jun 2016
 - initial release 
 - disabled variables change highlight

## Roadmap

![travis](https://img.shields.io/travis/Blazemeter/jmeter-debugger.svg)
![travis](https://img.shields.io/codecov/c/github/Blazemeter/jmeter-debugger.svg)

 - logic to stop on Listener bp only once after sample 
 - choose TG from context of tree and show debugger started? 
 - revive Stop button?
 - Maximize on Windows / Non modal window (what to do with all the collision cases?) 
 
 - italic font in tree not shown on Mac
 - blue color for current element is blue-on-blue for Mac
 - shortcut to toggle BP
 
 
