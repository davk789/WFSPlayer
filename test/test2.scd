/**
	simple sound file player
*/

~mixer = WFSMixer();


//  load the preset here

// Mixer
~scheowBus = ~mixer.getInBus(0);
~thunkBus = ~mixer.getInBus(1);

// Synth
~buffer1 = Buffer.read(s, "~/Music/Ableton Live Recordings/scheow.aif".standardizePath);
~buffer2 = Buffer.read(s, "~/Music/Ableton Live Recordings/thunk.aif".standardizePath);

s.queryAllNodes

~scheowPlayer = Synth.new('be_soundFile', ['outBus', ~scheowBus, 'buf', ~buffer1.bufnum], s, \addToHead);
~thunkPlayer = Synth.new('be_soundFile', ['outBus', ~thunkBus, 'buf', ~buffer2.bufnum], s, \addToHead);


// SynthDefs



SynthDef(\be_soundFile, { |buf=1, outBus=2|
	var source, env;
	source=PlayBuf.ar(1, buf);
	env=EnvGen.kr(Env.linen(1,BufDur.kr(buf),1,0.8),doneAction: 2);
	Out.ar(outBus,source*env);
}).load(s);







SynthDef(\sound, { |buf=1, outBus=50|
	var source, env;
	
	source=PlayBuf.ar(1, buf);
	env=EnvGen.kr(Env.linen(1,BufDur.kr(buf),1,0.8),doneAction: 2);
	Out.ar(outBus,source*env);
}).load(s);

SynthDef(\filter, {|lp=330 hp=339, inBus=50, outBus=20|
	var low, high;
	low = LPF.ar(In.ar(inBus),  Line.kr(20000,lp,7));
	high = HPF.ar(low, Line.kr(33,hp,7));
	Out.ar(outBus, high*Line.kr(0.5,0.9,7));
}).send;
