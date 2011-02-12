WFSInterface {
	/**
		WFSInterface -- This class serves these purposes:
		 - container for the GUI widgets
		 - perform actions to send to the engine -- including unit conversions
		 - store the parameter data in the running class
		   (there may need to be a separate preset class to read and write preferences)
	  */

	var activeChannel=0;    // index of the active source channel
	var channelCounter=0;   // counter value for use with the channel display only
	var parent;
	var sequencer;

	// GUI elements
	var controlViewWindow, initRow, globalRow, channelRow, transportRow; // containers for contolViewWidgets
	var globalWidgets, channelWidgets; // all gui elements are kept in a Dict for easy access

	// parameter defaults and storage
	var defaultChannelWidgetValues;
	var channelWidgetValues;

	*new { |par|
		^super.new.init_wfsinterface(par);
	}
	
	init_wfsinterface { |par|
		// member data
		/* 	get the stuff from the environment -- error handling would be a wise
			choice in a situation with multiple developers */
		parent = par; // get reference to the containing class
		sequencer = parent.sequencer; // create a local represenation of the sequencer

		globalWidgets = Dictionary();
		channelWidgets = Dictionary();
		defaultChannelWidgetValues =  Dictionary[
		    'channelLabel'            -> ("Channel " ++ channelCounter),
			'audioSourceMenu'         -> 0, 
			'channelLoopButton'       -> 0,  
			'channelVolumeBox'        -> -6,
			'channelXPositionBox'     -> 0.1,
			'channelYPositionBox'     -> 0.1,
			// channel sequencer params
			'channelRecordButton'     -> 0, // this and the recordMode are converted
			'channelRecordModeButton' -> 1, // to bool when used
			'channelPlayButton'       -> 0,
			'channelSequenceMenu'     -> 0,
		];

		// startup functions
		this.makeGUI;
		// setting the action must be done after the gui is initialized
		this.makeSequencerAction;
		
		postln(this.class.asString ++ " initialized");
	}

	makeSequencerAction {
		sequencer.action = { |val|
			// watch out for problems from scope
			defer{
				globalWidgets['locationMarkerArea'].setValueForIndex(activeChannel, val);
				// push the values to the top-level namespace here
			};
		}; 
	}

	getParam { |param|
		// trying to clean up the code a little, here. adding a shortcut method
		// to access the parameter value for the current channel
		^channelWidgetValues[activeChannel][param];
	}

	setParam { |param, val|
		// appropriate setter method
		channelWidgetValues[activeChannel][param] = val;
	}

	loadActiveChannel { |channelNum=0|
		var values, onMove, isRecording;
		/*********** this is called from the parent, which has a function of the same name */
		/**
			This is basically called when the GUI needs updating by adding a channel or
			clicking on the marker area
			It is passed the active channel
			number and performs these actions:
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
		// fill the sequence selection menu with the correct number of entries
		this.updateSequencerMenu;
		
		// prepare for recording if the onMove flag is set
		onMove = this.getParam('channelRecordModeButton').toBool;
		isRecording = this.getParam('channelRecordButton').toBool;

		if(onMove && isRecording){
			sequencer.prepareRecording(activeChannel);
		};
		
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
			channelWidgetValues = channelWidgetValues.add(defaultChannelWidgetValues.copy);

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
			parent.loadActiveChannel(channelWidgetValues.lastIndex);
		};


		// no matter what, it is okay to call this function
		sequencer.addChannel;

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
		
		channelWidgetValues = [defaultChannelWidgetValues.copy];
		
		// initialize the first channel in the MarkerArea to the value from the number box
		globalWidgets['locationMarkerArea'].value = [
			channelWidgetValues[0]['channelXPositionBox'] @	channelWidgetValues[0]['channelYPositionBox']
		];

		channelWidgets['channelDisplay'].items = ["Channel 1"];

		channelWidgets.do{ |obj|
			obj.enabled = true;
		};

		globalWidgets['locationMarkerArea'].enabled = true;

		parent.loadActiveChannel(0);
	}

	removeChannel { |chan|
		var markers, displayValues, chanToKill;

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

			// remove the sole corresponding channel from the sequencer
			sequencer.removeChannel; // use the default argument
		} // else
		{
			// if there is no arg, get the last channel available
			// engine sequencer and interface should follot this standard
			chanToKill = chan ?? { channelWidgetValues.lastIndex };
			
			// remove the current channel values, 
			channelWidgetValues.removeAt(chanToKill);

			// remove the marker from the markerArea
			markers = globalWidgets['locationMarkerArea'].value;
			markers.removeAt(chanToKill);
			globalWidgets['locationMarkerArea'].value = markers;
			
			// remove the entry from the menu
			displayValues = channelWidgets['channelDisplay'].items;
			displayValues.removeAt(chanToKill);
			channelWidgets['channelDisplay'].items = displayValues;

			// remove the corresponding channel from the sequencer
			sequencer.removeChannel(chanToKill);
			
			// activate the first channel of the project
			parent.loadActiveChannel(0);
		};

	}

	sendSequencerData {
		var isRecording, val;
		/**
			send data to the sequencer if recording is enabled on the current channel.
			note: this includes redundant access of the marker area. I think that the
			performance hit will be negligible, though.
		  */
		
		isRecording = this.getParam('channelRecordButton').toBool;

		if(isRecording){
			// I *think* calling getValueForIndex is significantly faster than
			// taking from the full value of the marker area, unless my test is flawed
			val = globalWidgets['locationMarkerArea'].getValueForIndex(activeChannel);

			sequencer.addEvent(activeChannel, val);
		};
	}

	setSoundLocation { |markerAreaVal|
		var val, isRecording;
		/**
			This is called by the MarkerArea. It performs these steps:
			1 - store the value in the list of Dictionaries containing the interface parameters
			2 - update the NumberBoxes to reflect the value (from the stored value)
			3 - push the updated data to the engine (not implemented yet)
			4 - push the updated data directly to the sequencer
		*/

		// notice that activeChannel is already set from the activate function. So, this
		// function can rely on the value implicitly.
		val  = markerAreaVal[activeChannel];
		
		// store the value
		this.setParam('channelXPositionBox', val.x);
		this.setParam('channelYPositionBox', val.y);
		
		// retrieve it from storage to set the NumberBoxes
		channelWidgets['channelXPositionBox'].value = this.getParam('channelXPositionBox');
		channelWidgets['channelYPositionBox'].value = this.getParam('channelYPositionBox');

		// .. and now push the value out to the engine
		// ... still need to implement this

		// and push the value to the sequencer
		this.sendSequencerData;
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

		// push the data to the sequencer
		this.sendSequencerData;
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
		// ... not implemented yet
		// push the data to the sequencer
		this.sendSequencerData;
	}

	setChannelLabel { |label|
		var menuItems;
		/**
			Stores the custom label for each channel, and change the menu label to match.
		*/
		menuItems = channelWidgets['channelDisplay'].items;
		menuItems[activeChannel] = label;
		channelWidgets['channelDisplay'].items = menuItems;

		this.setParam('channelLabel', label);
	}
	
	setChannelVolume { |vol|
		// store the volume parameter and push the value to the engine
		this.setParam('channelVolumeBox', vol);

		/*
			push the value to the engine -- the conversion from db to amp should be handled either
			in the top-level class or in the engine I think. the interface code should not
			be concerned with the values that are used by the engine.
		*/
	}

	setChannelAudioSource { |source|
		// again -- this functionality needs to be implemented in the engine
		this.setParam('audioSourceMenu', source);
	}

	setChannelRecord { |val|
		/**
			called from the record button
			- store the channel record flag
			- start recording if the onMove flag is false
			- perform the recording stop actions
		  */
		var record, onMove;

		this.setParam('channelRecordButton', val);
		record = this.getParam('channelRecordButton').toBool;
		
		onMove = this.getParam('channelRecordModeButton').toBool;
		
		case{record && onMove.not}{
			// initialize the recording
			sequencer.prepareRecording(activeChannel);
		}
		{record.not}{
			this.updateSequencerMenu;
		};
	}

	setChannelPlay { |val|
		var play, seq;

		this.setParam('channelPlayButton', val);
		play = this.getParam('channelPlayButton').toBool;

		seq = this.getParam('channelSequenceMenu');
		
		if(play){
			sequencer.playSequence(activeChannel, seq);
		}{
			sequencer.stop; // this is going to need to handle channels next
		};
	}

	updateSequencerMenu {
		var numSeqs;
		/* perform the cleanup actions -- update the gui, basically */
		// for now, don't make it possible to name the sequences, maybe that can be added later
		numSeqs = sequencer.sequences[activeChannel].size;

		channelWidgets['channelSequenceMenu'].items = Array.fill(numSeqs, { |ind|
			"Sequence " ++ (ind + 1);
		});
	}

	setChannelRecordOnMove { |val|
		// set a flag so that the interface knows when to establish the beginning
		// of a recording
		this.setParam('channelRecordModeButton', val);
	}

	setActiveSequence { |val|
		// store the value only
		this.setParam('channelSequenceMenu', val);
	}
	
	makeGUI {
		var scrollingNBColor = Color.new255(255, 255, 200);

		controlViewWindow = Window("WFS Mixer", Rect(500.rand, 500.rand, 1000, 485)).front;
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
			    .action_({ parent.addChannel; })
		);
		// marker area

		globalWidgets = globalWidgets.add(
			'locationMarkerArea' -> WFSMarkerArea(controlViewWindow, Rect(0, 0, 475, 475))
			    .canAddMarker_(false)
			    .mouseDownAction_({ |obj| parent.loadActiveChannel(obj.currentIndex); })
			    .mouseMoveAction_({ |obj| this.setSoundLocation(obj.value); });
		);
		
		// channel control row

		channelRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		// grr i wish there was a way to auto generate these controls
		channelWidgets = channelWidgets.add(
			'channelDisplay' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .action_({ |obj| parent.loadActiveChannel(obj.value); });
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
			    .action_({ parent.removeChannel(activeChannel); })
		);
		
		// transport row
		
		transportRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		// hmm... might as well add labels wherever possible
		StaticText(transportRow, Rect(0, 0, 0, 20))
		    .string_("sequences")
		    .stringColor_(Color.white);

		// ** this will hold the list of sequences
		channelWidgets = channelWidgets.add(
			'channelSequenceMenu' -> PopUpMenu(transportRow, Rect(0, 0, 0, 20))
			    .action_({ |obj| this.setActiveSequence(obj.value); });
		);
		
		StaticText(transportRow, Rect(0, 0, 0, 20))
		    .string_("transport")
		    .stringColor_(Color.white);


		// better to avoid abstracting this set of buttons, get rid of this
		// transport view
		/*
		channelWidgets = channelWidgets.add(
			'channelTransportView' -> WFSVTransportView(transportRow, Rect(0, 0, 0, 180))
			    .recordAction_({ |obj| this.setChannelRecord(obj) });
			);*/

		StaticText(transportRow, Rect(0, 0, 0, 80))
		    .string_("add buttons at the same time as their functions!")
		    .font_(Font("Arial Narow", 12))
		    .stringColor_(Color.white);

		channelWidgets = channelWidgets.add(
			'channelPlayButton' -> Button(transportRow, Rect(0, 0, 0, 20))
		    .states_([[">", Color.white, Color.black], [">", Color.black, Color.white]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				this.setChannelPlay(obj.value);
			});
		);

		channelWidgets = channelWidgets.add(
			'channelStopButton' -> Button(transportRow, Rect(0, 0, 0, 20))
		        .states_([["[]", Color.white, Color.black]])
		        .font_(Font("Arial Black", 12))
		        .action_({ |obj|
					channelWidgets['channelPlayButton'].valueAction = 0;
					channelWidgets['channelRecordButton'].valueAction = 0;
					// -- probably doesn't need its own function
				});
		);
		
		channelWidgets = channelWidgets.add(
			'channelRecordButton' -> Button(transportRow, Rect(0, 0, 0, 20))
			    .states_([["o", Color.red, Color.black], ["o", Color.black, Color.red]])
			    .font_(Font("Arial Black", 12))
			    .action_({ |obj| this.setChannelRecord(obj.value) });
		);
		

		channelWidgets = channelWidgets.add(
			'channelRecordModeButton' -> Button(transportRow, Rect(0, 0, 0, 20))
		        .states_([
					["record on move", Color.yellow, Color.black],
					["record on move", Color.black, Color.yellow]
				])
		        .value_(1)
		        .action_({ |obj| this.setChannelRecordOnMove(obj.value) });
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