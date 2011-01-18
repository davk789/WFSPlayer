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

	var activeChannel=0; // index of the active source channel
	var numChannels=8;   // local representation of the number of sound sources
	var parent;

	// GUI elements
	var controlViewWindow, initRow, globalRow, channelRow; // containers for contolViewWidgets
	var globalWidgets, channelWidgets; // all gui elements are kept in a Dict for easy access
	// for now, the values of the channels should be kept in a list of Dictionaries
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
		// initialize the bank of values based on the default GUI values
		this.initChannelWidgetValues;
		
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
		
		if(channelNum == activeChannel){
			^nil; // break out of the function
		};

		// this is important -- the functions called by marker area and position number boxes
		// rely on this member variable.
		activeChannel = channelNum;

		channelWidgets['channelDisplay'].value = activeChannel;

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

	setSoundLocation { |markerAreaVal|
		var val;
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
	
	makeGUI {
		var scrollingNBColor = Color.new255(255, 255, 200);

		controlViewWindow = Window("WFS Mixer", Rect(500.rand, 500.rand, 900, 485)).front;
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
			'globalPlayButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([[">", Color.black, Color.white]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		globalWidgets = globalWidgets.add(
			'globalPauseButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["||", Color.black, Color.yellow]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		globalWidgets = globalWidgets.add(
			'globalStopButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["[]", Color.white, Color.black]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		globalWidgets = globalWidgets.add(
			'globalRecordButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["O", Color.black, Color.red]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("master volume (dB)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'masterVolumeBox' -> WFSScrollingNumberBox(globalRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .background_(scrollingNBColor);
		);
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("number of channels")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(	
			'numChannelsBox' -> NumberBox(globalRow, Rect(0, 0, 0, 20))
			    .value_(numChannels);
		);
		// marker area

		globalWidgets = globalWidgets.add(
			'locationMarkerArea' -> WFSMarkerArea(controlViewWindow, Rect(0, 0, 475, 475))
			    .canAddMarker_(false)
			    .value_(Array.fill(numChannels, { |ind|
					(ind / (numChannels - 1)) @ 0.1
				}))
			    .mouseDownAction_({ |obj| this.activateChannel(obj.currentIndex); })
			    .mouseMoveAction_({ |obj| this.setSoundLocation(obj.value); });
		);
		
		// channel control row

		channelRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		// grr i wish there was a way to auto generate these controls
		channelWidgets = channelWidgets.add(
			'channelDisplay' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .items_(Array.fill(numChannels, { |ind|
					"Channel " ++ (ind + 1)
				}))
			    .value_(0)
			    .action_({ |obj|
					var val = obj.value;
					globalWidgets['locationMarkerArea'].currentIndex = val;
					this.activateChannel(val);
				})
			    .stringColor_(Color.white);
		);

		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("audio source")
		    .stringColor_(Color.white);

		channelWidgets = channelWidgets.add(
			'audioSourceMenu' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .items_(["select...", "Audio In 1", "Audio In 2", "Audio In etc", "Sampler 1", "SynthDef 1"]);
		);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("transport")
		    .stringColor_(Color.white);
		
		channelWidgets = channelWidgets.add(
			'channelPlayButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([[">", Color.black, Color.white]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelWidgets = channelWidgets.add(
			'channelPauseButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["||", Color.black, Color.yellow]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelWidgets = channelWidgets.add(
			'channelStopButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["[]", Color.white, Color.black]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelWidgets = channelWidgets.add(
			'channelRecordButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["O", Color.black, Color.red]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelWidgets = channelWidgets.add(
			'channelSequenceClearButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["clear sequence", Color.black, Color.grey]]);
		);
		
		channelWidgets = channelWidgets.add(
			'channelLoopButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["loop", Color.black, Color.grey]]);
		);
	
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("volume (dB)")
		    .stringColor_(Color.white);
	
		channelWidgets = channelWidgets.add(
			'channelVolumeBox' -> WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .background_(scrollingNBColor);
		);
		// implement the marker area first before these
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("x-position (0..1)")
		    .stringColor_(Color.white);
		
		channelWidgets = channelWidgets.add(
			'channelXPositionBox' -> NumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.2)
			    .action_({ |obj| this.setChannelXPosition(obj.value); });
		);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("y-position (0..1)")
		    .stringColor_(Color.white);

		channelWidgets = channelWidgets.add(
			'channelYPositionBox' -> NumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.2)
			    .action_({ |obj| this.setChannelYPosition(obj.value) });
		);
		
		channelWidgets = channelWidgets.add(
			'channelSaveButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		);

		channelWidgets = channelWidgets.add(
			'channelLoadButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
		);	

	}

	initChannelWidgetValues {
		// the interface contains a list of dictionaries that hold the values for each channel.
		// when activating the channel, the widgets will persist, and their values will only be
		// updated. there should be a check to make sure that these values and their engine
		// counterparts are syncronized
		channelWidgetValues = Array.fill(numChannels, { |ind|
			Dictionary[
				'audioSourceMenu'     -> channelWidgets['audioSourceMenu'].value,
				'channelLoopButton'   -> channelWidgets['channelLoopButton'].value,
				'channelVolumeBox'    -> channelWidgets['channelVolumeBox'].value,
				'channelXPositionBox' -> globalWidgets['locationMarkerArea'].value[ind].x,
				'channelYPositionBox' -> globalWidgets['locationMarkerArea'].value[ind].y,
			];
		});

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