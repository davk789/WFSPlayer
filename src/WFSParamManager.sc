WFSParamManager { // "controller" class
	var environment;
	var synthChannels;
	var gui;
	var synthParams, interfaceParams;
	/*
		this class should be responsible for these things:
		- it should send control messages from the gui and the sequencer to the 
		  channel objects
		- it should update the gui when the sequencer changes the channel objects

		the delay calculation may need to go here, actually.
	*/
	
	*new {
		^super.new.init_wfsparammanager;
	}

	init_wfsparammanager {
		// initialize local data
		synthParams = Dictionary();
		interfaceParams = Dictionary();
	}

	loadEnvironment { |env|
		// load the environment data after everything is in place
		environment = env;
		synthChannels = env.channels;
		gui = env.gui;
	}

	moveSoundSource { |markerArea|
		// i never finished this so i should write out this function later
		var chan = markerArea.currentIndex;
		//		var markers = markerArea.coords;

		//		synthChannels[chan].x_(markers[chan].x).y_(markers[chan].y);
	}

	makeChannelActive { |markerArea|
		var activeChannel;
		activeChannel = markerArea.currentIndex;
		gui.loadActiveChannel(activeChannel);
	}

	getChannelControlValues { |channelNum|
		var channelControlValues;
		channelControlValues = this.convertParameters(synthChannels[channelNum].params);
		^channelControlValues;
	}

	convertParameters { |params|
		// take the synth channel values and convert them to a dictionary
		// that will set the channel controls
	}

	setSynthParam { |param...args|
		param.switch( // do unit conversions here
			'masterVolume', {
				//  this would be more efficient to send one argument
				// to the enclosing group but then the different instances will
				// not store the data // WFSSynthChannel
				synthChannels.do{ |obj,ind|
					obj.setLevel(args[0].dbamp);
				};
				
			},
			'speakerSpacing', {
				// yeah....
			},
			'numSpeakers', {
				
				// there is a side effect of the channels getting reloaded
				// completely, also re-initialized
				environment.numChannels = args[0]; // number of audio channels not voices

			},
			'airTemperature', { // doesn't push to the separate channels
				synthChannels.do{ |obj,ind|
					obj.airTemperature = args[0];
				};
			},
			'roomWidth', {
				postln("ignoring this key -- num speakers * speaker spacing should suffice.");
			},
			'roomDepth', {
				// convert distance to delay time
				synthChannels.do{ |obj,ind|
					obj.maxDelay = args[0] * 12; // feet to inches
				};
			},
			'channelVolume', {
				synthChannels[args[0]].setLevel(args[1].dbamp);
			},
			'xPosition', {
				synthChannels[args[0]].x = args[1];
			},
			'yPosition', {
				synthChannels[args[0]].y = args[1];
			},
			{ error("the caller does not match a method."); }
		);
		synthChannels.do{ |obj,ind|
			postln("here are all the parameters");
			obj.params.postln;
		};
	}

	setGUIControl {
		// interact with the sequencer?
	}
}