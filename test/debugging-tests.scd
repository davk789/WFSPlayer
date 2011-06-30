/**
	Random code snippets for testing WFSMixerFeatures.
*/


~main_na = WFSMixer("test");

~main_na

~main_na.startAction = { |obj,chan| ["start", obj,chan].postln; };
~main_na.action = { |obj,chan,loc| ["move", obj,chan,loc].postln; };
~main_na.action = {}

