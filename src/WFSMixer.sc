WFSMixer {
	/**
		WFSMixer - a simple spatializing mixer.
		
		This class will and should use two sublasses -- an interface class and an engine class.
		The engine class will act as a container for the channel subclasses.
	*/
	var s;
	var <numChannels=16; // number of speakers, i.e. number of output channels
	                     // ** to be used by the subordinate classes
	var <>engine;        // the engine -- the container for the channel synth classes
	var <>interface;     // the interface class
	
	*new { 
		^super.new.init_wfsmixer;
	}
	
	init_wfsmixer {
		s = Server.default;
		
		// should presets be handled here?
		
		engine = WFSEngine(this); // passing the value from the top-level class
										 // ** the top-level class may hold the data that passes 
										 // between interface and engine
		interface = WFSInterface(this);      //

		// all done, alert the post window
		postln(this.class.asString ++ " initialized");
	}


	loadActiveChannel { |chan|
		// jump up to container classes, and then call the subordinate classes'
		// respective loadActiveChannel() functions
		engine.loadActiveChannel(chan);
		interface.loadActiveChannel(chan);
	}
	
}

