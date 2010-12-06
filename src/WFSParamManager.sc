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
				postln("i don't know what to do please help me.");
				//				this.setChannelVolume(args[0], args[1]);				
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
		synthChannels[args[0]].params.postln;
	}

	setGUIControl {
		// interact with the sequencer?
	}
}