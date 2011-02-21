WFSObject {
	/**
		stub class for all classes called by WFSMixer. Maybe there is something else
		appropriate to add here as well.
	*/
	var parent;

	*new {
		^super.new.init_wfsobject;
	}

	init_wfsobject {
		postln(this.class.asString ++ " initialized");
	}

	getEnvironment { |par|
		/* pass the enclosing envronment to the object */
		parent = par;
	}

	// maybe throw subclassResponsibility errors later, for now, allow execution to
	// continue hitting this error
	
	loadPreset {
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