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
}