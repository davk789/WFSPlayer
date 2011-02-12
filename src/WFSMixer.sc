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
	var <>engine;        // has all the audio classes -- sources and mixer channels
	var <>interface;
	var <>sequencer;
	
	*new { 
		^super.new.init_wfsmixer;
	}
	
	init_wfsmixer {
		s = Server.default;
		
		// watch for dependency errors
		sequencer = WFSSequencer(this); // the sequencer is not accessing the parent yet.

		engine = WFSEngine(this); // passing the value from the top-level class
										 // ** the top-level class may hold the data that passes 
										 // between interface and engine
		interface = WFSInterface(this);      //

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
		if(chan.isNil){
			// issue a warning for a missing argument
			error("Channel number argument is needed to remove a channel!")
		};
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

