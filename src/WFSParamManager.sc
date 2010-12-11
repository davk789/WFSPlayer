WFSParamManager { // "controller" class
	var environment;
	var synthChannels;
	var synthParams, interfaceParams;
	/*
		this class should translate between the params, and the delay times. 
		so, the delay calculations should go here, actually.
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
				// oh yeah ... 
			},
			'airTemperature', { // doesn't push to the separate channels
				synthChannels.do{ |obj,ind|
					obj.airTemperature = args[0];
				};
			},
			'roomWidth', {
				postln("ignoring this key -- num speakers * speaker spacing should suffice.");
			},
			'roomDepth', { // neither does this
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