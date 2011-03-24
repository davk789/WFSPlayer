WFSMixer {
	/**
		WFSMixer - a simple spatializing mixer.

		This class creates and facilitates communication between the various objects in 
		the project.
	*/
	var s;
	var <>engine;
	var <>interface;
	var <>sequencer;
	var <>preferences;
	
	*new { |test|
		^super.new.init_wfsmixer(test);
	}
	
	init_wfsmixer { |conditions|
		s = Server.default;
		
		preferences = WFSPreferences();
		sequencer = WFSSequencer();
		engine = WFSEngine();
		interface = WFSInterface();

		// break early for testing
		if(conditions == "test"){
			this.initializeDeferred;
			^nil;
		};

		if(s.serverRunning){
			s.quit;
		};

		// for testing -- when playing itunes through the headphones, specify
		// the edirol
		if(conditions == "edirol"){
			s.options.device = ServerOptions.devices[0];
		};
		
		// the delay for the channels exceeds the default limit for server memory 
		// allocation. 
		s.options.memSize = 2 ** 18; // ~256 MB (8MB is the default)
		s.waitForBoot{ this.initializeDeferred; };
				
		// all done, alert the post window
		postln(this.class.asString ++ " initialized");
	}

	initializeDeferred {
		/**
			deferred initialization -- this comes after the server is booted, and after preliminary
			initialization for all classes has been finished. Therefore, any data that relies on
			other objects or on the running server should be initialized in a function that is
			called here.

		*/
		//		s.sendMsg('dumpOSC', 1);
		
		preferences.getEnvironment(this); // doesn't use the environment yet but will soon
		// the sequencer now calls the interface directly
		// and then calls a callback function, rather than providing the interface with
		// a callback that will then be used only to update the gui
		sequencer.getEnvironment(this);
		engine.getEnvironment(this); // engine does not use the environment yet
		interface.getEnvironment(this);
		// initialize local representation of the environment for the objects
		interface.initDeferred;
		preferences.initDeferred;
		engine.initDeferred;
	}
	
	addChannel {
		sequencer.addChannel;
		interface.addChannel;
		engine.addChannel;
		// return the input bus of the new channel
		^engine.getAllInputs.last;
	}

	removeChannel { |chan|
		sequencer.removeChannel(chan);
		engine.removeChannel(chan);
		interface.removeChannel(chan);
	}
	
	loadActiveChannel { |chan|
		engine.loadActiveChannel(chan);
		interface.loadActiveChannel(chan);
	}

	loadPreset { |filename|
		var values;
		// this breaks the pattern of simply making the same call across the various
		// subordinate classes. re-name the methods here?
		values = preferences.loadPreset(filename);
		sequencer.loadPreset(values[2]);
		interface.loadPreset(values[0], values[1]); // interface depends on the sequencer for menu data
		engine.loadPreset; // build all the values from the interface
	}

	// aliases for moveAction
	action {
		^sequencer.moveAction;
	}

	action_ { |func|
		sequencer.moveAction = func;
	}

	startAction {
		^sequencer.startAction;
	}

	startAction_ { |func|
		sequencer.startAction = func;
	}
	
	stopAction {
		^sequencer.stopAction;
	}

	stopAction_ { |func|
		sequencer.stopAction = func;
	}	

	moveAction {
		^sequencer.moveAction;
	}

	moveAction_ { |func|
		sequencer.moveAction = func;
	}
	
	// interface functions

	getInBus { |chan|
		^engine.getInBus(chan);
	}
	
}