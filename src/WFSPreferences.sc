WFSPreferences {
	/**
		Store and retrieve preferences files. 
		(This will be closely tied to the interface class, but should still
		be instantiated at the top level.)
	  */
	*new {
		^super.new.init_wfspreferences;
	}

	init_wfspreferences {
		postln(this.class.asString ++ " initialized");
	}

	storeDefault {
		// store a single value for persistent configuration data
	}

	storePreset {
		// store preset file -- i.e. all the data in the class
	}

	retrievePreset {
		// retrieve preset
	}
}