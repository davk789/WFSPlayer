WFSInterface : WFSObject {
	/**
		This class handles all user input, stores the front-end parameter information, and interacts
		with the sequencer.
	*/

	var activeChannel=0;    // index of the active source channel
	var channelCounter=0;   // counter value for use with the channel display only
	// GUI elements
	var controlViewWindow, initRow, globalRow, channelRow, transportRow;
	var <globalWidgets, <channelWidgets; // all gui elements are kept in a Dict for easy access

	// parameter defaults and storage
	var defaultChannelWidgetValues;
	var <channelWidgetValues, <globalWidgetValues;

	*new {
		^super.new.init_wfsinterface;
	}
	
	init_wfsinterface {
		globalWidgets = Dictionary();
		channelWidgets = Dictionary();
		globalWidgetValues = Dictionary[
			'numSpeakersBox'  -> 16,
			'airTempBox'      -> 75,
			'roomWidthBox'    -> 25,
			'roomDepthBox'    -> 35,
			'masterVolumeBox' -> -6,
		];
		defaultChannelWidgetValues =  Dictionary[
		    'channelLabel'               -> ("Channel " ++ channelCounter),
			//'audioSourceMenu'          -> 0, // access to the mixer is through code for now
			'channelLoopButton'          -> 0,  
			'channelVolumeBox'           -> -6,
			'channelXPositionBox'        -> 0.1,
			'channelYPositionBox'        -> 0.1,
			'channelInvertDelayButton'   -> 0,
			// channel sequencer params
			'channelRecordButton'        -> 0, // this and the recordMode are converted
			'channelRecordModeButton'    -> 1, // to bool when used
			'channelRecordMonitorButton' -> 1,
			'channelPlayButton'          -> 0,
			'channelSequenceMenu'        -> 0,
		];
		
		postln(this.class.asString ++ " initialized");
	}

	initDeferred {		
		// startup functions
		// this relies on the sequencer, so, put it in initDeferred
		this.makeGUI;

		globalWidgetValues.keysValuesDo{ |key,val|
			globalWidgets[key].value = val;
		}
	}

	// called directly by the sequencer
	updateMove { |index, val| // aka moveAction
		globalWidgets['locationMarkerArea'].setValueForIndex(index, val);
		engine.updateLocation(index, val);
	}

	updateStop { |index| // any args that need to be passed?
		var stopCondition;
		stopCondition =
		    channelWidgetValues[index]['channelLoopButton'].toBool
		    && channelWidgetValues[index]['channelPlayButton'].toBool;
		if(stopCondition){
			sequencer.playSequence(index, channelWidgetValues[index]['channelSequenceMenu']);
		}{
			channelWidgetValues[index]['channelPlayButton'] = 0;
			
			if(index == activeChannel){
				channelWidgets['channelPlayButton'].value = 0;
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
		var values, onMove, isRecording, alreadyRecording;
		/**
			This is basically called when the GUI needs updating by adding a channel or
			clicking on the marker area
			It is passed the active channel
			number and performs these actions:
			 - set the activeChannel member variable for subsequent use by this class
			 - update the value of the channel gui elements based on the array of
			   dictionaries that store the parameters for all the channels
		*/

		activeChannel = channelNum;

		// *** both of the following GUI elements call loadActiveChannel
		// so, they are setting themselves in this function. watch for problems.
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
		alreadyRecording = sequencer.isRecording(activeChannel);
		
		if(onMove && isRecording && alreadyRecording.not){
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
			channelWidgetValues.last['channelLabel'] = ("Channel " ++ channelCounter);
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
		channelWidgetValues.first['channelLabel'] = ("Channel " ++ channelCounter);
		
		
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

			// sequencer.loadActiveChannel(chanToKill);
			// used to be called here
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
		engine.updateLocation(activeChannel, val);

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
		engine.updateLocation(activeChannel, markerVals[activeChannel]);
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
		engine.updateLocation(activeChannel, markerVals[activeChannel]);
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
		this.setParam('channelVolumeBox', vol);
		engine.setChannelVolume(activeChannel, this.getParam('channelVolumeBox'));
	}

	setChannelInvertDelay { |choice|
		// engine accesses this value, no need to push to engine
		this.setParam('channelInvertDelayButton', choice);
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

		if(record){
			this.setChannelPlay(0);
			if(onMove.not){
				sequencer.prepareRecording(activeChannel);
			}
		}{
			// will this confict with the normal playback?
			sequencer.stop(activeChannel);
			this.updateSequencerMenu;
		};
	}

	setChannelPlay { |val|
		var play, seq;

		// disallow playback if there is a recording happening already
		if(sequencer.isRecording(activeChannel)){
			channelWidgets['channelPlayButton'].value = 0;
			^nil;
		};
		
		// if there are no sequences in the current channel, then
		// break out of the function
		if(channelWidgets['channelSequenceMenu'].items.isNil
			|| channelWidgets['channelSequenceMenu'].items.size == 0){
			channelWidgets['channelPlayButton'].value = 0;
			^nil;
		};
		
		this.setParam('channelPlayButton', val);
		play = this.getParam('channelPlayButton').toBool;

		seq = this.getParam('channelSequenceMenu');
		
		if(play){
			sequencer.playSequence(activeChannel, seq);
		}{
			postln("stopping the sequencer from the interface.");
			sequencer.stop(activeChannel);
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

	setChannelRecordMonitor { |val|
		this.setParam('channelRecordMonitorButton', val);
	}
	
	setActiveSequence { |val|
		// store the value only
		this.setParam('channelSequenceMenu', val);
	}

	setChannelLoop { |val|
		this.setParam('channelLoopButton', val);
	}
	
	// global functions
	
	setGlobalPlay {
		
		channelWidgetValues.do{ |obj,ind|
			var seq;

			obj['channelPlayButton'] = 1;

			seq = channelWidgetValues[ind]['channelSequenceMenu'];
			sequencer.playSequence(ind, seq);
		};

		channelWidgets['channelPlayButton'].value = 1;
		
	}

	setGlobalStop {
		channelWidgetValues.do{ |obj,ind|
			obj['channelPlayButton'] = 0;
			sequencer.stop(ind);
		};

		channelWidgets['channelPlayButton'].value = 0;
	}

	savePreset {
		prefManager.save;
		globalWidgets['presetListMenu'].items = prefManager.getPresetList;
	}

	loadPreset { |data, globalData|
		var values, numSequences;

		// gather the data
		data.postln;
		channelWidgetValues = data;
		globalWidgetValues = globalData;

		channelWidgetValues.do{ |obj|
			postf("the channel delay invert value is %\n", obj['channelInvertDelayButton']);
		};
		// update the interface

		globalWidgetValues.keysValuesDo{ |key, val|
			globalWidgets[key].value = val;
		};
		
		channelWidgets['channelDisplay'].items = channelWidgetValues.collect{ |obj|
			obj['channelLabel'];
		};

		globalWidgets['locationMarkerArea'].value = channelWidgetValues.collect{ |obj|
			obj['channelXPositionBox'] @ obj['channelYPositionBox'];
		};

		globalWidgets['locationMarkerArea'].enabled = true;

		channelCounter = channelWidgetValues.size;

		numSequences = sequencer.sequences[activeChannel].size;
		channelWidgets['channelSequenceMenu'].items = Array.fill(numSequences, { |ind|
			"Sequence " ++ (ind + 1);
		});

		channelWidgets.do{ |obj|
			obj.enabled = true;
		};

		parent.loadActiveChannel(0);
	}

	// global param stuff

	setNumSpeakers { |num|
		globalWidgetValues['numSpeakersBox'] = num;
		engine.numChannels = globalWidgetValues['numSpeakersBox'];
	}

	setAirTemp { |temp|
		globalWidgetValues['airTempBox'] = temp;
		engine.airTemperature = globalWidgetValues['airTempBox'];
	}
	
	setRoomWidth { |width|
		globalWidgetValues['roomWidthBox'] = width;
		engine.roomWidth = globalWidgetValues['roomWidthBox'];
	}

	setRoomDepth { |depth|
		globalWidgetValues['roomDepthBox'] = depth;
		engine.roomDepth = globalWidgetValues['roomDepthBox'];
	}

	setMasterVolume { |vol|
		globalWidgetValues['masterVolumeBox'] = vol;
		engine.masterVolume = globalWidgetValues['masterVolumeBox'];
	}

	// barf the gui
	
	makeGUI {
		var presetList;
		var scrollingNBColor = Color.new255(255, 255, 200);

		controlViewWindow = Window("WFSMixer", Rect(500.rand, 500.rand, 1000, 485)).front;
		controlViewWindow.view.decorator = FlowLayout(controlViewWindow.view.bounds);
		
		initRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		// settings row
		
		globalWidgets = globalWidgets.add(
			'presetSaveButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]])
			    .action_({ this.savePreset; });
		);

		globalWidgets = globalWidgets.add(
			'presetListMenu' -> PopUpMenu(initRow, Rect(0, 0, 0, 20))
			    .background_(Color.white.alpha_(0.6))
			    .items_(prefManager.getPresetList);
		);
				
		globalWidgets = globalWidgets.add(
			'presetLoadButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]])
			    .action_({ parent.loadPreset(globalWidgets['presetListMenu'].item); });
		);		

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("number of speakers")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'numSpeakersBox' ->	NumberBox(initRow, Rect(0, 0, 0, 20))
			     .value_(engine.numChannels) // there, now that top-level value is used :P
			     .action_({ |obj| this.setNumSpeakers(obj.value) });
		);
		
		/*
			let's assume for now that the width refers to the outermost speakers, and
			all other speakers are equally spaced between the ends.
			StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("spacing (inches)")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'speakerSpacingBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(18)
			    .background_(scrollingNBColor);
		);
		*/
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("air temp. (farenheit)")
			.stringColor_(Color.white);

		globalWidgets = globalWidgets.add(
			'airTempBox' -> NumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(75)
			    .action_({ |obj| this.setAirTemp(obj.value); });
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room width (feet)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'roomWidthBox' -> NumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(20)
			    .action_({ |obj| this.setRoomWidth(obj.value); });
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room depth (feet)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'roomDepthBox' -> NumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(100)
			    .action_({ |obj| this.setRoomDepth(obj.value); });
		);
		
		// global control row
		
		globalRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("global transport")
			.stringColor_(Color.white);

		/*
		globalWidgets = globalWidgets.add(
			'globalTransportView' -> WFSVTransportView(globalRow, Rect(0, 0, 0, 180));
		);
		*/

		globalWidgets = globalWidgets.add(
			'globalPlayButton' -> Button(globalRow, Rect(0, 0, 0, 20))
		        .states_([[">", Color.blue(0.45), Color.white]])
		        .font_(Font("Arial Black", 12))
			    .action_({ |obj| this.setGlobalPlay; });
		);
				
		globalWidgets = globalWidgets.add(
			'globalStopButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["[]", Color.white, Color.blue(0.45)]])
		        .font_(Font("Arial Black", 12))
			    .action_({ |obj| this.setGlobalStop; });
		);
				
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("master volume (dB)")
			.stringColor_(Color.white);
		
		globalWidgets = globalWidgets.add(
			'masterVolumeBox' -> WFSScrollingNumberBox(globalRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .background_(scrollingNBColor)
			    .action_({ |obj| this.setMasterVolume(obj.value); });
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
			    .mouseDownAction_({ |obj|
					parent.loadActiveChannel(obj.currentIndex);
				})
			    .mouseMoveAction_({ |obj|
					this.setSoundLocation(obj.value);
					if(this.getParam('channelRecordMonitorButton').toBool){
						sequencer.moveAction.value(parent, activeChannel, obj.currentValue);
					};
				});
		);
		
		// channel control row

		channelRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		// grr i wish there was a way to auto generate these controls
		channelWidgets = channelWidgets.add(
			'channelDisplay' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .background_(Color.white.alpha_(0.6))
			    .action_({ |obj| parent.loadActiveChannel(obj.value); });
		);

		channelWidgets = channelWidgets.add(
			'channelLabel' -> TextField(channelRow, Rect(0, 0, 0, 20))
			    .string_("Channel 1")
			    .action_({ |obj| this.setChannelLabel(obj.value); });
		);
		
		/*		StaticText(channelRow, Rect(0, 0, 0, 20))
		    .string_("audio source")
		    .stringColor_(Color.white);

		channelWidgets = channelWidgets.add(
			'audioSourceMenu' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .items_(["select...", "Audio In 1", "Audio In 2", "Audio In etc", "Sampler 1", "SynthDef 1"])
			    .action_({ |obj| this.setChannelAudioSource(obj.value) });
		);
		*/
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
			'channelInvertDelayButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([
					["invert delay", Color.white, Color.black],
					["invert delay", Color.black, Color.yellow]
				])
			    .action_({ |obj| this.setChannelInvertDelay(obj.value); })
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
			    .background_(Color.white.alpha_(0.6))
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


		channelWidgets = channelWidgets.add(
			'channelSequenceClearButton' -> Button(transportRow, Rect(0, 0, 0, 20))
			    .states_([["clear sequence", Color.black, Color.grey]]);
		);
		
		channelWidgets = channelWidgets.add(
			'channelLoopButton' -> Button(transportRow, Rect(0, 0, 0, 20))
			    .states_([
					["loop", Color.black, Color.grey],
					["loop", Color.black, Color.yellow]
				])
			    .action_({ |obj| this.setChannelLoop(obj.value); });
		);
		
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
					// call the callback if the sequencer is not playing this channel
					if(sequencer.isPlaying(activeChannel).not
						&& this.getParam('channelRecordMonitorButton').toBool){
						sequencer.stopAction.value(parent,activeChannel);
					};
					channelWidgets['channelPlayButton'].valueAction = 0;
					channelWidgets['channelRecordButton'].valueAction = 0;
				});
		);
		
		channelWidgets = channelWidgets.add(
			'channelRecordButton' -> Button(transportRow, Rect(0, 0, 0, 20))
			    .states_([["o", Color.red, Color.black], ["o", Color.black, Color.red]])
			    .font_(Font("Arial Black", 12))
			    .action_({ |obj|
					this.setChannelRecord(obj.value);
					if(obj.value == 1
						&& this.getParam('channelRecordMonitorButton').toBool){
						// pass along the callbacks
						sequencer.startAction.value(parent, activeChannel);
					};
				});
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

		channelWidgets = channelWidgets.add(
			'channelRecordMonitorButton' -> Button(transportRow, Rect(0, 0, 0, 20))
			    .states_([
					["record monitor", Color.black, Color.grey],
					["record monitor", Color.black, Color.yellow]
				])
			    .value_(1)
			    .action_({ |obj| this.setChannelRecordMonitor(obj.value); });
		);

		// disable the channel controls until a sound source is added
		globalWidgets['locationMarkerArea'].enabled = false;
		
		channelWidgets.do{ |obj|
			obj.enabled = false;
		};
		
	}

}