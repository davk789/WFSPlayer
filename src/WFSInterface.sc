WFSInterface {
	/**
		WFSInterface -- This class serves these purposes:
		 - container for the GUI widgets
		 - perform actions to send to the engine -- including unit conversions
		 - store the parameter data in the running class
		   (there may need to be a separate preset class to read and write preferences)

		** NOTE: this class depends on its container for functionality and cannot be called
		   on its own
	  */

	var activeChannel=0;    // index of the active source channel
	var channelCounter=0;   // counter value for use with the channel display only
	var parent;

	// GUI elements
	var controlViewWindow, initRow, globalRow, channelRow, transportRow; // containers for contolViewWidgets
	var globalWidgets, channelWidgets; // all gui elements are kept in a Dict for easy access
	/*
		NOTE: channelWidgetValues gets initialized and deinitialized with the add
		and remove functions, rather than getting initialized in the init_wfsinterface function
		and simply being emptied when removing the last channel.
	*/
	var channelWidgetValues;

	*new { |par|
		^super.new.init_wfsinterface(par);
	}
	
	init_wfsinterface { |par|
		// member data
		parent = par; // get reference to the containing class
		globalWidgets = Dictionary();
		channelWidgets = Dictionary();

		// startup functions
		this.makeGUI;
		
		postln(this.class.asString ++ " initialized");
	}

	loadActiveChannel { |channelNum=0|
		var values;

		/**
			this function is called by the enclosing class. It is passed the active channel
			number and performs these actions:
			 - break out of the function if called on the current active channel (redundancy check)
			 - set the activeChannel member variable for subsequent use by this class
			 - update the value of the channel gui elements based on the array of
			   dictionaries that store the parameters for all the channels
		*/

		// this is important -- the functions called by marker area and position number boxes
		// rely on this member variable.
		activeChannel = channelNum;

		// *** both of the following GUI elements call loadActiveChannel
		// so, somehow these calls should be filtered. but for now, the redundancy
		// should be less of a problem than forcing extra calls for everything that
		// uses this function (gui actions, add/remove channels/preset load calls, etc.)
		channelWidgets['channelDisplay'].value = activeChannel;
		globalWidgets['locationMarkerArea'].currentIndex = activeChannel;

		channelWidgetValues[activeChannel].keysValuesDo{ |key,val|
			channelWidgets[key].value = val;
		};

	}
	
	activateChannel { |chan|
		/**
			call on the parent class to load the active channel for all relevant subclasses, including
			this one. pass the active channel index to daddy. loadActiveChannel will then be called
			from parent.
			
		*/
		parent.loadActiveChannel(chan);
	}

	addChannel {
		var markers, channelMenuItems;
		/**
			if there are no channels yet, do the inialization action.
			if there are channels,
			- increment the channel counter
			- add the values to channelWidgetValues
			- add a node to the MarkerArea
			- add the choice to the selection menu
			- activate the newly created channel
		*/
		if(channelWidgetValues.isNil){
			this.initializeChannels;
		}{ // else
			channelCounter = channelCounter + 1;

			// add a set of values to channelWidgetValues
			channelWidgetValues = channelWidgetValues.add(
				Dictionary[
					'channelLabel'        -> ("Channel " ++ channelCounter),
					'audioSourceMenu'     -> 0, 
					'channelLoopButton'   -> 0,  
					'channelVolumeBox'    -> -6,
					'channelXPositionBox' -> 0.1,
					'channelYPositionBox' -> 0.1,
				];
			);

			// add a marker to locationMarkerArea
			/*
				this always adds the new point to a single location (0.1 @ 0.1). it may be nice
				to have handle the locatin more intelligently, so that the channels do not overlap
				if you hit "add channel" more than once without moving the point.... maybe later
			*/
			markers = globalWidgets['locationMarkerArea'].value;
			markers = markers.add(
				channelWidgetValues[channelWidgetValues.lastIndex]['channelXPositionBox']
			        @
				channelWidgetValues[channelWidgetValues.lastIndex]['channelYPositionBox']
			);
			globalWidgets['locationMarkerArea'].value = markers;

			// add an item to channelLabel
			channelMenuItems = channelWidgets['channelDisplay'].items;
			channelMenuItems = channelMenuItems.add("Channel " ++ channelCounter);
			channelWidgets['channelDisplay'].items = channelMenuItems;

			// activate the newly created value
			this.activateChannel(channelWidgetValues.lastIndex); 
		};

	}

	initializeChannels {
		/*
			initialization tasks performed here:
			- set the numChannels member variable to 1
			- initialize channelWidgetValues and the value of globalWidgets['locationMarkerArea'].
			- initialize the value of the channel selection menu
			- activate the channel controls.
		*/

		// initialize the channel counter
		channelCounter = 1;

		// channelWidgetValues is an array of dictionaries, holding values with the same keys
		// as their GUI counterparts. this simplifies the loadChannel function
		
		channelWidgetValues = [
			Dictionary[
				'channelLabel'        -> "Channel 1",
				'audioSourceMenu'     -> 0,
				'channelLoopButton'   -> 0,
				'channelVolumeBox'    -> -6,
				'channelXPositionBox' -> 0.1,
				'channelYPositionBox' -> 0.1,
			];
		];
		
		// initialize the first channel in the MarkerArea to the value from the number box
		globalWidgets['locationMarkerArea'].value = [
			channelWidgetValues[0]['channelXPositionBox'] @	channelWidgetValues[0]['channelYPositionBox']
		];

		channelWidgets['channelDisplay'].items = ["Channel 1"];

		channelWidgets.do{ |obj|
			obj.enabled = true;
		};

		globalWidgets['locationMarkerArea'].enabled = true;

		this.activateChannel(0);
	}

	removeChannel {
		var markers, displayValues;

		case{channelWidgetValues.isNil}{
			// nothing to de-initialize, break out of the function
			^nil;
		} // else if
		{ channelWidgetValues.size == 1 }{
			// set channelWidgetValues to nil
			channelWidgetValues = nil;
			// empty the display values
			channelWidgets['channelDisplay'].value = 0;
			channelWidgets['channelDisplay'].items = Array();
			
			// empty the marker area
			globalWidgets['locationMarkerArea'].value = Array();
			
			// disable the GUI elements
			channelWidgets.do{ |obj|
				obj.enabled = false;
				globalWidgets['locationMarkerArea'].enabled = false;
			};
		} // else
		{
			// remove the current channel values, 
			channelWidgetValues.removeAt(activeChannel);

			// remove the marker from the markerArea
			markers = globalWidgets['locationMarkerArea'].value;
			markers.removeAt(activeChannel);
			globalWidgets['locationMarkerArea'].value = markers;
			
			// remove the entry from the menu
			displayValues = channelWidgets['channelDisplay'].items;
			displayValues.removeAt(activeChannel);
			channelWidgets['channelDisplay'].items = displayValues;

			// activate the first channel of the project
			this.activateChannel(0);
		};

	}

	setSoundLocation { |markerAreaVal|
		var val, xPos, yPos;
		/**
			This is called by the MarkerArea. It performs these steps:
			1 - store the value in the list of Dictionaries containing the interface parameters
			2 - update the NumberBoxes to reflect the value (from the stored value)
			3 - push the updated data to the engine
		*/

		// notice that activeChannel is already set from the activate function. So, this
		// function can rely on the value implicitly.
		val  = markerAreaVal[activeChannel];
		
		// store the value
		channelWidgetValues[activeChannel]['channelXPositionBox'] = val.x;
		channelWidgetValues[activeChannel]['channelYPositionBox'] = val.y;

		
		// retrieve it from storage to set the NumberBoxes
		channelWidgets['channelXPositionBox'].value = channelWidgetValues[activeChannel]['channelXPositionBox'];
		channelWidgets['channelYPositionBox'].value = channelWidgetValues[activeChannel]['channelYPositionBox'];

		// .. and now push the value out to the engine
		// ... still need to implement this
	}

	setChannelLabel { |label|
		/**
			Stores the custom label for each channel, and that's it.
			  ... maybe do something more with this later.
		*/
		channelWidgetValues[activeChannel]['channelLabel'] = label;
	}

	setChannelXPosition { |val|
		var pos, markerVals;
		/**
			this is called by the ChannelXPosition number box and performs these actions:
			1 - catch garbage values typed in to the number box
			2 - store the values in the list of Dicts
			3 - update the MarkerArea to reflect the new values
			4 - push the data to the engine
		*/
		case{ val < 0 }{
			pos = 0;
			channelWidgets['channelXPositionBox'].value = 0;
		} // else if
		{ val > 1 }{
			pos = 1;
			channelWidgets['channelXPositionBox'].value = 1;		
		} // else
		{
			pos = val;
		};
		
		// remember -- .value returns a representation of the MerkerArea's value
		// so, I need to get the value, alter and re-set the value
		markerVals = globalWidgets['locationMarkerArea'].value;
		markerVals[activeChannel].x = pos;
		
		globalWidgets['locationMarkerArea'].value = markerVals;

		// push the data to the engine
	}

	setChannelYPosition { |val|
		var pos, markerVals;
		/**
			this is called by the ChannelYPosition number box and performs the same actions as the
			X position version. I am leaving it like like this, with the redundant lines, in order
			to preserve readability.
		*/
		case{ val < 0 }{
			pos = 0;
			channelWidgets['channelYPositionBox'].value = 0;
		} // else if
		{ val > 1 }{
			pos = 1;
			channelWidgets['channelYPositionBox'].value = 1;		
		} // else
		{
			pos = val;
		};
		
		markerVals = globalWidgets['locationMarkerArea'].value;
		markerVals[activeChannel].y = pos;
		
		globalWidgets['locationMarkerArea'].value = markerVals;

		// push the data to the engine
		// ** maybe both all the MarkerArea updates can call one funtction
		// dealing with the push stuff later, though
	}

	setChannelVolume { |vol|
		// store the volume parameter and push the value to the engine
		channelWidgetValues[activeChannel]['channelVolumeBox'] = vol;

		/*
			push the value to the engine -- the conversion from db to amp should be handled either
			in the top-level class or in the engine I think. the interface code should not
			be concerned with the values that are used by the engine.
		*/
	}

	setChannelAudioSource { |source|
		// again -- this functionality needs to be implemented in the engine
		channelWidgetValues[activeChannel]['audioSourceMenu'] = source;
	}
	
	makeGUI {
		var scrollingNBColor = Color.new255(255, 255, 200);

		controlViewWindow = Window("WFS Mixer", Rect(500.rand, 500.rand, 1100, 485)).front;
		controlViewWindow.view.decorator = FlowLayout(controlViewWindow.view.bounds);
		
		initRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		// settings row
		
		globalWidgets = globalWidgets.add(
			'presetSaveButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		);
		
		globalWidgets = globalWidgets.add(
			'presetLoadButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
		);		
	
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("number of speakers")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'numSpeakersBox' ->	NumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(16);
		);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("spacing (inches)")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'speakerSpacingBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(18)
			    .background_(scrollingNBColor);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("air temp. (farenheit)")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'airTempBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(75)
			    .background_(scrollingNBColor);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room width (feet)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'roomWidthBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(20)
			    .background_(scrollingNBColor);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room depth (feet)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'roomDepthBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(100)
			    .background_(scrollingNBColor);
		);
		
		// global control row
		
		globalRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("global transport")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'globalTransportView' -> WFSVTransportView(globalRow, Rect(0, 0, 0, 180));
		);
				
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("master volume (dB)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'masterVolumeBox' -> WFSScrollingNumberBox(globalRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .background_(scrollingNBColor);
		);

		globalWidgets = globalWidgets.add(
			'addChannelButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["add channel", Color.black, Color.grey]])
			    .action_({ this.addChannel; })
		);
		// marker area

		globalWidgets = globalWidgets.add(
			'locationMarkerArea' -> WFSMarkerArea(controlViewWindow, Rect(0, 0, 475, 475))
			    .canAddMarker_(false)
			    /*.value_(Array.fill(numChannels, { |ind| // empty to begin with
					(ind / (numChannels - 1)) @ 0.1
				}))*/
			    .mouseDownAction_({ |obj| this.activateChannel(obj.currentIndex); })
			    .mouseMoveAction_({ |obj| this.setSoundLocation(obj.value); });
		);
		
		// channel control row

		channelRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		// grr i wish there was a way to auto generate these controls
		channelWidgets = channelWidgets.add(
			'channelDisplay' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .action_({ |obj| this.activateChannel(obj.value); });
		);

		channelWidgets = channelWidgets.add(
			'channelLabel' -> TextField(channelRow, Rect(0, 0, 0, 20))
			    .string_("Channel 1")
			    .action_({ |obj| this.setChannelLabel(obj.value); });
		);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("audio source")
		    .stringColor_(Color.white);

		channelWidgets = channelWidgets.add(
			'audioSourceMenu' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .items_(["select...", "Audio In 1", "Audio In 2", "Audio In etc", "Sampler 1", "SynthDef 1"])
			    .action_({ |obj| this.setChannelAudioSource(obj.value) });
		);
		
		channelWidgets = channelWidgets.add(
			'channelSequenceClearButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["clear sequence", Color.black, Color.grey]]);
		);
		
		channelWidgets = channelWidgets.add(
			'channelLoopButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([
					["loop", Color.black, Color.grey],
					["loop", Color.black, Color.yellow]
				]);
		);
	
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("volume (dB)")
		    .stringColor_(Color.white);
	
		channelWidgets = channelWidgets.add(
			'channelVolumeBox' -> WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .background_(scrollingNBColor)
			    .action_({ |obj| this.setChannelVolume(obj.value) });
		);
		// implement the marker area first before these
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("x-position (0..1)")
		    .stringColor_(Color.white);
		
		channelWidgets = channelWidgets.add(
			'channelXPositionBox' -> NumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.1)
			    .action_({ |obj| this.setChannelXPosition(obj.value); });
		);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("y-position (0..1)")
		    .stringColor_(Color.white);
		channelWidgets = channelWidgets.add(
			'channelYPositionBox' -> NumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.1)
			    .action_({ |obj| this.setChannelYPosition(obj.value) });
		);

		channelWidgets = channelWidgets.add(
			'removeChannelButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["remove channel", Color.red, Color.black]])
			    .action_({ this.removeChannel; })
		);
		
		// transport row
		/**
			I need to figure out how this will be used here.
		  */
		
		transportRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
			
		StaticText(transportRow, Rect(0, 0, 0, 20))
		    .string_("transport")
		    .stringColor_(Color.white);
		
		channelWidgets = channelWidgets.add(
			'channelTransportView' -> WFSVTransportView(transportRow, Rect(0, 0, 0, 180));
		);

		// disable the channel controls until a sound source is added
		globalWidgets['locationMarkerArea'].enabled = false;
		
		channelWidgets.do{ |obj|
			obj.enabled = false;
		};


		/*
			// leave out the presets per-channel for now
			// depending on how the usage develops it will probably
			// be better to keep the presets limited to the global settings
		channelWidgets = channelWidgets.add(
			'channelSaveButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		);

		channelWidgets = channelWidgets.add(
			'channelLoadButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
			);	*/

	}

	setNumChannels { |num|
		/**
			prepare the gui to handle the number of active channels.
			- set the instance variable, 
			- communicate to the top-level class
			- update the marker area to reflect the number of sound sources
		  */
		/*var numChan;

		parent.numChannels = num;

		numChan = parent.numChannels;

		globalWidgets['locationMarkerArea'].value = Array.fill(numChan, { |ind|
			(ind / (numChan - 1)) @ 0.1;
		});
		
		globalWidgets['locationMarkerArea'].refresh;
		*/	
	}


}