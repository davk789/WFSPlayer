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
		// laod the environment data after everythin is in place
		environment = env;
		synthChannels = env.channels;
	}

	setSynthParam { |param...args|
		param.switch(
			'channelVolume', {
				this.setChannelVolume(args[0], args[1]);
			},
			{ error("the caller does not match a method."); }
		);
		// take the coordinates and calculate a delay
	}

	setChannelVolume { |channel, val|
		synthChannels[channel].setParam('lev', val.dbamp);
	}

	setGUIControl {
		// interact with the sequencer?
	}
}