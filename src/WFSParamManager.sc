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
		param.switch(
			'masterVolume', {
				synthChannels[args[0]].setLevel(args[1].dbamp);
			},
			'channelVolume', {
				postln("i don't know what to do please help me.");
				//				this.setChannelVolume(args[0], args[1]);
			},
			'xPosition', {
				synthChannels[args[0]].setXPosition(args[1]);
			},
			'yPosition', {
				synthChannels[args[0]].setYPosition(args[1]);
			},
			{ error("the caller does not match a method."); }
		);
	}

	setGUIControl {
		// interact with the sequencer?
	}
}