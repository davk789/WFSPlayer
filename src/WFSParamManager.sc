WFSParamManager { // "controller class" ... ?
	var environment, synthChannels;
	var synthParams, interfaceParams;
	/*
		this class should translate between the params, and the delay times. 
		so, the delay calculations should go here, actually.
	*/
	
	*new { |env|
		^super.new.init_wfsparammanager(env);
	}

	init_wfsparammanager { |env|
		environment = env;
		synthChannels = env.channels;
		synthParams = Dictionary();
		interfaceParams = Dictionary();
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
		synthChannels.setParam('lev', val.ampdb);
	}

	setGUIControl {
		// interact with the sequencer?
	}
}