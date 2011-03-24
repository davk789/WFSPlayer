WFSObject {
	/**
		stub class for all classes called by WFSMixer. Maybe there is something else
		appropriate to add here as well.
	*/
	var parent;
	var prefManager, sequencer, engine, interface;
	*new {
		^super.new.init_wfsobject;
	}

	init_wfsobject {
		postln(this.class.asString ++ " initialized");
	}

	getEnvironment { |par|
		/* pass the enclosing envronment to the object */
		postln("passing the environment in " ++ this.class.asString);
		parent = par;
		// this function is called by each class, so one of these definitions will 
		// be self-referential. I think that "this" and "sequencer" in WFSSequencer
		// will be identical (for instance) and that will be the only side effect.
		prefManager = parent.preferences;
		sequencer = parent.sequencer;
		engine = parent.engine;
		interface = parent.interface;
		postf("% % % %\n", prefManager, sequencer, engine, interface);
	}

	// maybe throw subclassResponsibility errors later, for now, allow execution to
	// continue hitting this error
	
	loadPreset {
		warn(thisMethod.asString ++ " should have been implemented by " ++ this.class.asString);
		^nil;
	}

	initDeferred {
		warn(thisMethod.asString ++ " should have been implemented by " ++ this.class.asString);
		^nil;
	}
	
	// channel update methods to be called from the top-level class

	addChannel {
		warn(thisMethod.asString ++ " should have been implemented by " ++ this.class.asString);
		^nil;
	}

	removeChannel {
		warn(thisMethod.asString ++ " should have been implemented by " ++ this.class.asString);
		^nil;
	}

	loadActiveChannel {
		warn(thisMethod.asString ++ " should have been implemented by " ++ this.class.asString);
		^nil;
	}
}