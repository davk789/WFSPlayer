WFSMixer {
	/**
		WFSMixer - a simple spatializing mixer.

		The top level class does these things:
		- instantiate the objects that will perform all the work
		- allow for communication between objects through this top-level class
		- synchronize function calls that must affect all object in the project
	*/
	var s;
	var <numChannels=16; // number of speakers, i.e. number of output channels
	                     // ** to be used by the subordinate classes
	/**** 
		currently missing the number of input channels -- this data is only kept in the interface 
		at the moment -- this data needs to be used by the engine as well
	*/ 
	var <>engine;        // 
	var <>interface;
	var <>sequencer;
	var <>preferences;   // do all of these *really* need read and write prefrences?
	
	*new { 
		^super.new.init_wfsmixer;
	}
	
	init_wfsmixer {
		s = Server.default;
		
		preferences = WFSPreferences();		
		sequencer = WFSSequencer();
		engine = WFSEngine();
		interface = WFSInterface();
		
		if(s.serverRunning){
			this.initializeDeferred;
		}{
			s.waitForBoot{ this.initializeDeferred; };
		};
				
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

		preferences.getEnvironment(this); // doesn't use the environment yet but will soon
		//sequencer.getEnvironment(this); // sequencer doesn't need the environment
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
	}

	numChannels_ { |num|
		numChannels = num;
		engine.numChannels = numChannels;
	}
	
}