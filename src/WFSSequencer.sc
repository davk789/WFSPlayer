WFSSequencer {
	/*
		not clear yet whether this will be one class, or a set of classes with a 
		top-level administrator class.
	*/
	*new {
		^super.new.init_wfssequencer;
	}

	init_wfssequencer {
		postln(this.class.asString ++ " initialized");
	}
}