WFSGUI {
	var controlViewWindow, initRow, globalRow, channelRow;
	var mixerViewWin; // this will hold the mixer view
	var controlViewWidgets, channelControlWidgets, mixerViewWidgets;
	var activeChannel=0;
	var paramManager;

	*new { |man|
		^super.new.init_wfsgui(man);
	}
	
	init_wfsgui { |man|
		paramManager = man;
		controlViewWidgets = Dictionary();
		mixerViewWidgets = Dictionary();
		this.makeControlView;
	}

	makeMixerView {
		// create a clobal mixer window, makeGUI style function here
	}

	addMixerChannel {
		// add a channel to the mixer, same type of init function
	}

	removeMixerChannel {
		// remove the channel
	}

	setActiveChannel {
		// more work here, this updates the active channel controls and connects
		// to the proper mixer synth channel
	}

	makeControlView {
		controlViewWindow = Window("WFS Mixer", Rect(500.rand, 500.rand, 900, 485)).front;
		controlViewWindow.view.decorator = FlowLayout(controlViewWindow.view.bounds);
		
		initRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		// settings row
		
		controlViewWidgets = controlViewWidgets.add(
			'presetSaveButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		);
		
		controlViewWidgets = controlViewWidgets.add(
			'presetLoadButton' -> Button(initRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
		);		
	
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("number of speakers")
			.stringColor_(Color.white);

		controlViewWidgets = controlViewWidgets.add(
			'numSpeakersBox' ->	WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(16);
		);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("spacing (inches)")
			.stringColor_(Color.white);

		controlViewWidgets = controlViewWidgets.add(
			'speakerSpacingBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(18);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("air temp. (farenheit)")
			.stringColor_(Color.white);

		controlViewWidgets = controlViewWidgets.add(
			'airTempBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(75);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room width (feet)")
			.stringColor_(Color.white);
		
		controlViewWidgets = controlViewWidgets.add(
			'roomWidthBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(20);
		);

		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room depth (feet)")
			.stringColor_(Color.white);
		
		controlViewWidgets = controlViewWidgets.add(
			'roomDepthBox' -> WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			    .value_(100);
		);
		
		// global control row
		
		globalRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("global transport")
			.stringColor_(Color.white);

		controlViewWidgets = controlViewWidgets.add(
			'globalPlayButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([[">", Color.black, Color.white]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		controlViewWidgets = controlViewWidgets.add(
			'globalPauseButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["||", Color.black, Color.yellow]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		controlViewWidgets = controlViewWidgets.add(
			'globalStopButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["[]", Color.white, Color.black]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		controlViewWidgets = controlViewWidgets.add(
			'globalRecordButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_([["O", Color.black, Color.red]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("master volume (dB)")
			.stringColor_(Color.white);
		
		controlViewWidgets = controlViewWidgets.add(
			'masterVolumeBox' -> WFSScrollingNumberBox(globalRow, Rect(0, 0, 0, 20))
			    .value_(-6);
		);
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("add/remove points")
			.stringColor_(Color.white);
		
		controlViewWidgets = controlViewWidgets.add(	
			'editBypassButton' -> Button(globalRow, Rect(0, 0, 0, 20))
			    .states_(
				    [
					    ["on", Color.black, Color.green],
				    	["off", Color.green, Color.black]
				    ]
			);
		);
		// marker area

		controlViewWidgets = controlViewWidgets.add(
			'locationMarkerArea' -> WFSMarkerArea(controlViewWindow, Rect(0, 0, 475, 475));
		);
		
		// channel controls

		channelRow = VLayoutView(controlViewWindow, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));

		this.loadActiveChannel; // let's not load the active channel by default
	
	}

	loadActiveChannel { |channelNum=0|
		
		activeChannel = channelNum;

		// remove the old channel
		if(channelControlWidgets.notNil){
			channelControlWidgets.do{ |obj, ind|
				obj.remove;
			};
			channelControlWidgets = nil;
		};

		// initialize and add the channel dict
		channelControlWidgets = Dictionary();

		// ** the controls need to get the params from the param manager

		// the labels need to be added to the dictionary so that they can be removed
		// grr i wish there was a way to auto generate these controls
		channelControlWidgets = channelControlWidgets.add(
			'lab1' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("channel " ++ (channelNum + 1))
			    .stringColor_(Color.white);
		);

		channelControlWidgets = channelControlWidgets.add(
			'lab10' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("audio source")
			    .stringColor_(Color.white);
		);		

		channelControlWidgets = channelControlWidgets.add(
			'audioSourceMenu' -> PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			    .items_(["select...", "Audio In 1", "Audio In 2", "Audio In etc", "Sampler 1", "SynthDef 1"]);
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'lab2' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("transport")
			    .stringColor_(Color.white);
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelPlayButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([[">", Color.black, Color.white]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelPauseButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["||", Color.black, Color.yellow]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelStopButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["[]", Color.white, Color.black]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelRecordButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["O", Color.black, Color.red]])
			    .font_(Font.new("Arial Black", 12));
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelSequenceClearButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["clear sequence", Color.black, Color.grey]]);
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelLoopButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["loop", Color.black, Color.grey]]);
		);
	
		channelControlWidgets = channelControlWidgets.add(
			'lab3' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("volume (dB)")
			    .stringColor_(Color.white);
		);
	
		channelControlWidgets = channelControlWidgets.add(
			'channelVolumeBox' -> WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(-6)
			    .action_({ |obj| 
					paramManager.setSynthParam('channelVolume', activeChannel, obj.value);
				});
		);

		channelControlWidgets = channelControlWidgets.add(
			'lab4' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("x-position (0..1)")
			    .stringColor_(Color.white);
		);
		
		channelControlWidgets = channelControlWidgets.add(
			'channelXPositionBox' -> WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.2);
		);

		channelControlWidgets = channelControlWidgets.add(
			'lab5' -> StaticText(channelRow, Rect(0, 0, 0, 20))
			    .string_("y-position (0..1)")
			    .stringColor_(Color.white);
		);

		channelControlWidgets = channelControlWidgets.add(
			'channelYPositionBox' -> WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			    .value_(0.2);
		);		
		
		channelControlWidgets = channelControlWidgets.add(
			'channelSaveButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		);

		channelControlWidgets = channelControlWidgets.add(
			'channelLoadButton' -> Button(channelRow, Rect(0, 0, 0, 20))
			    .states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
		);
	
	}
}