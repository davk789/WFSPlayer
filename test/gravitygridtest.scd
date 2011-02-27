/**
	some code to create a synth that uses GravityGrid to test the WFSPlayer audio
  */
s.queryAllNodes


~main = WFSMixer();
~outBus = ~main.engine.getInBus(0);// I think I'll need something like this

~node = s.nextNodeID;

s.sendMsg('s_new', 'testWFS', ~node, 0, 1, 'outBus', ~outBus);
s.sendMsg('s_new', 'testWFS', ~node, 0, 1, 'outBus', 0);
s.sendMsg('n_free', ~node);

SynthDef.new("testWFS", { |outBus=20, lev=0.1|
	var aSig;
	aSig = GravityGrid.ar(
		Impulse.kr(LFNoise0.kr([0.25,0.231],90,100)),
		[100.2,10.5],
		LFNoise0.kr(10,0.8),
		LFNoise0.kr(10,0.8));
	Out.ar(outBus, Pan2.ar(aSig) * lev);
	//Out.ar(outBus, aSig * lev);
}).load(s);

/*
	// quick gravity grid templates from the help file

//GravityGrid comes out with some interesting noisy sounds- WARNING, loud, aliasing! You'll probably
want to filter the outputs, maybe with RLPF, Slew et al.

{Pan2.ar(0.05*GravityGrid.ar(Impulse.kr(2),MouseX.kr(0.1,10,'exponential'),LFNoise0.kr
(2,0.8),LFNoise0.kr(2,0.8)),0.0)}.play

{Pan2.ar(0.5*GravityGrid.ar(Impulse.kr(1),SinOsc.kr(0.5,0.0,0.8,1.0),LFSaw.kr(50),LFNoise0.kr
(10,0.8)),0.0)}.play

//rhythmic violence
{0.1*GravityGrid.ar(Impulse.kr(LFNoise0.kr([0.25,0.231],90,100)),[100.2,10.5], LFNoise0.kr(10,0.8),LFNoise0.kr(10,0.8))}.play

//noisy
{Pan2.ar(Resonz.ar(GravityGrid.ar(Impulse.kr(50),0.01,LFNoise0.kr(1,4),LFNoise0.kr
(1,4)),1000,0.1),0.0)}.play

//48k spectrum looping
{Pan2.ar(Resonz.ar(GravityGrid.ar(Impulse.kr(5),0.01,LFNoise0.kr(1,4),LFNoise0.kr
(1,4)),1000,0.1),0.0)}.play

b = Buffer.alloc(s, 9, 1); //weights buffer

b.setn(0,Array.rand(9,0.1,0.9)); //set weights

{Pan2.ar(0.1*GravityGrid.ar(Impulse.kr(1),MouseX.kr(0.1,1,'exponential'),LFNoise0.kr
(1,0.8),LFNoise0.kr(1,0.8), b.bufnum),0.0)}.play

b.setn(0,Array.rand(9,0.1,0.9)); //change weights to new random values

b.setn(0,[0.1,1,0.1,2,0.1,3,0.1,4,0.1]); //change weights to new specific values

{0.1*Resonz.ar(GravityGrid.ar(Impulse.kr(LFNoise0.kr([0.2,0.3],70,80)),[11.2,12.5], LFNoise0.kr
(5.4,0.4),LFNoise0.kr(10,0.8), b.bufnum),SinOsc.ar(0.03,0,300,500),SinOsc.ar(0.03,0,0.3,0.4))}.play

b.setn(0,[0.1,0,0.3,4,0.1,4,0.3,0,0.1]); 

b.setn(0,[4.1,0,6.3,4,0.1,4,0.3,0,7.1]); 

{0.05*GravityGrid.ar(Impulse.kr([2,4.1]),1, SinOsc.kr(1,pi,0.9), SinOsc.kr(1.9,0,0.9),b.bufnum)}.play

//violent
{Pan2.ar(0.1*GravityGrid.ar(Impulse.ar(0),MouseX.kr(0.01,10,'exponential')))}.play


*/