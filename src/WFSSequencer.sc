WFSSequencer {
	*new { 
		^super.new.init_wfsequencer;
	}

	init_wfsequencer { 
		postln(this.class.asString ++ " initialized");
	}
}