WFSParamManager { // "controller class" ... ?
	var environment;
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
		synthParams = Dictionary();
		interfaceParams = Dictionary();
	}

	setSynthParam {
		// take the coordinates and calculate a delay
	}

	setGUIControl {
		// interact with the sequencer?
	}
}