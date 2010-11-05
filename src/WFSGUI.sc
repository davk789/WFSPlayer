WFSGUI {
	*new {
		^super.new.init_wfsgui;
	}
	
	init_wfsgui {
		postln(this.class.asString ++ " initialized");
	}
}