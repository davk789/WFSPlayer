WFSGUI {
	var win, initRow, globalRow, channelRow;
	var channelNumber=1;
	*new {
		^super.new.init_wfsgui;
	}
	
	init_wfsgui {

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
		win = Window("WFS Mixer", Rect(500.rand, 500.rand, 900, 485)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		initRow = VLayoutView(win, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		// settings row
		
		Button(initRow, Rect(0, 0, 0, 20))
			.states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		
		Button(initRow, Rect(0, 0, 0, 20))
			.states_([["load", Color.white, Color.new255(150, 150, 255, 200)]]);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("number of speakers")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			.value_(16);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("spacing (inches)")
			.stringColor_(Color.white);
			
		WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			.value_(18);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("air temp. (farenheit)")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			.value_(75);
			
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room width (feet)")
			.stringColor_(Color.white);
			
		WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			.value_(20);
		
		StaticText(initRow, Rect(0, 0, 0, 20))
			.string_("room depth (feet)")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(initRow, Rect(0, 0, 0, 20))
			.value_(100);
		
		// global control row
		
		globalRow = VLayoutView(win, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("global transport")
			.stringColor_(Color.white);
		
		Button(globalRow, Rect(0, 0, 0, 20))
			.states_([[">", Color.black, Color.white]])
			.font_(Font.new("Arial Black", 12));
		
		Button(globalRow, Rect(0, 0, 0, 20))
			.states_([["||", Color.black, Color.yellow]])
			.font_(Font.new("Arial Black", 12));
		
		Button(globalRow, Rect(0, 0, 0, 20))
			.states_([["[]", Color.white, Color.black]])
			.font_(Font.new("Arial Black", 12));
		
		Button(globalRow, Rect(0, 0, 0, 20))
			.states_([["O", Color.black, Color.red]])
			.font_(Font.new("Arial Black", 12));
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("master volume (dB)")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(globalRow, Rect(0, 0, 0, 20))
			.value_(-6);
		
		StaticText(globalRow, Rect(0, 0, 0, 20))
			.string_("add/remove points")
			.stringColor_(Color.white);
		
		
		Button(globalRow, Rect(0, 0, 0, 20))
			.states_(
				[
					["on", Color.black, Color.green],
					["off", Color.green, Color.black]
				]
			);
		
		// marker area
		
		WFSMarkerArea(win, Rect(0, 0, 475, 475));
		
		// channel controls
		
		channelRow = VLayoutView(win, Rect(0, 0, 120, 475))
			.background_(Color.black.alpha_(0.8));
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("channel " ++ channelNumber)
			.stringColor_(Color.white);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("audio source")
			.stringColor_(Color.white);
		
		PopUpMenu(channelRow, Rect(0, 0, 0, 20))
			.items_(["select...", "Audio In 1", "Audio In 2", "Audio In etc", "Sampler 1", "SynthDef 1"]);
		
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("transport")
			.stringColor_(Color.white);
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([[">", Color.black, Color.white]])
			.font_(Font.new("Arial Black", 12));
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["||", Color.black, Color.yellow]])
			.font_(Font.new("Arial Black", 12));
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["[]", Color.white, Color.black]])
			.font_(Font.new("Arial Black", 12));
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["O", Color.black, Color.red]])
			.font_(Font.new("Arial Black", 12));
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["clear sequence", Color.black, Color.grey]]);
		
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["loop", Color.black, Color.grey]]);
	
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("volume (dB)")
			.stringColor_(Color.white);
	
		WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			.value_(-6);
			
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("x-position (0..1)")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			.value_(0.2);
		StaticText(channelRow, Rect(0, 0, 0, 20))
			.string_("y-position (0..1)")
			.stringColor_(Color.white);
		
		WFSScrollingNumberBox(channelRow, Rect(0, 0, 0, 20))
			.value_(0.7);
			
		Button(channelRow, Rect(0, 0, 0, 20))
			.states_([["save", Color.white, Color.new255(150, 150, 255, 200)]]);
		
	}
}