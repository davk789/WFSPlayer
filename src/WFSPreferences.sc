WFSPreferences {
	/**
		this class is responsible for one thing - formatting relevant class data
		for use with DOMDocument etc. classes.

		For now, this will be tied to the WFSPlayer class, but it might be possible
		to implement a simple protocol if it is only writing Dictionaries to xml
		files and loading xml files to dictionaries.
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