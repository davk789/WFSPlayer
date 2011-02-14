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
		
		// ** watch for dependency errors
		preferences = WFSPreferences(); 
		sequencer = WFSSequencer(); // the sequencer does not access the parent
		
		engine = WFSEngine(this);
		// the "demigod" class. this is where the magic happens.
		interface = WFSInterface(this);

		// all done, alert the post window
		postln(this.class.asString ++ " initialized");
	}

	// I see a pattern forming -- call the parent for functions that are replicated
	// in all of the other classes
	
	addChannel {
		sequencer.addChannel;
		engine.addChannel;
		interface.addChannel;
	}

	removeChannel { |chan|
		sequencer.removeChannel(chan);
		engine.removeChannel(chan);
		interface.removeChannel(chan);
	}
	
	loadActiveChannel { |chan|
		// jump up to container classes, and then call the subordinate classes'
		// respective loadActiveChannel() functions
		engine.loadActiveChannel(chan);
		interface.loadActiveChannel(chan);
	}
	
}

