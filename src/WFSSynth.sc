WFSSynth : Model {
	*new {
		^super.new.init_wfssynth;
	}
	
	init_wfssynth {
		postln(this.class.asString ++ " initialized");
	}
}