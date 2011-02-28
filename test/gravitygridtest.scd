/**
	some code to create a synth that uses GravityGrid to test the WFSPlayer audio
*/
s.quit;
s.boot;
s.queryAllNodes;

~testClass = Proto({
	~inBusCounter = 20;
	~defaultParams = Dictionary[
		'delayTime' -> 0.01,
		'maxDelay'  -> 1,    // corresponds to "room size" in the interface
		'lev'       -> 1,    // per-synth calculated attenuation
		'gain'      -> 1,    // global gain attenuation
		'outBus'    -> 0,
		'inBus'     -> 20,
	];
	~numChannels = 2;
	~groupNode = s.nextNodeID;
	~synthNodes = Array();
	~params = Array();
	~addChannel = {
		/*
			prepare the data storage and create synths on the server.
		*/
		var newNodes = Array();
		var newParams = Array();
		s.sendMsg('g_new', ~groupNode, 1, 1);
		~numChannels.do{ |ind|
			newNodes = newNodes.add(s.nextNodeID);
			newParams = newParams.add(~defaultParams.copy);
			newParams.last['outBus'] = ind;
			newParams.last['inBus'] = ~inBusCounter;
			newParams.last['delayTime'] = 0.9.rand + 0.1; // ** test
			// i don't know whiy this doesn't work -- the params are not being set
			// even though the synth is being created
			s.listSendMsg(
				['s_new', 'WFSMixerChannelTest', newNodes.last, 0, ~groupNode]
				++
				newParams.last.getPairs
			);
			s.sendMsg('n_set', newNodes.last, 'gate', 1);
		};
		~inBusCounter = ~inBusCounter + 1;
		// store the nodes here for later setting methods
		~synthNodes = ~synthNodes.add(newNodes);
		~params = ~params.add(newParams);
		s.queryAllNodes;
	};
	~getInBus = { |chan|
		~params[chan][0]['inBus'];
	};
});

s.sendMsg('dumpOSC', 1);
s.sendMsg('dumpOSC', 0);
~testClass.addChannel;

// this works
~outBus = ~testClass.getInBus(0);
~outBus = 20;

// this doesn't work
~main = WFSMixer();
~outBus = ~main.engine.getInBus(0);// I think I'll need something like this

~node = s.nextNodeID;
s.sendMsg('s_new', 'testWFS', ~node, 0, 1, 'outBus', ~outBus);
s.sendMsg('s_new', 'testWFS', ~node, 0, 1, 'outBus', 0);
s.sendMsg('n_free', ~node);

s.sendMsg('n_set', 1001, 'outBus', 0, 'delayTime', 1);
s.sendMsg('n_set', 1002, 'outBus', 1, 'delayTime', 0.1);

~mNode = s.nextNodeID;
~mNode2 = s.nextNodeID;
s.sendMsg('s_new', 'WFSMixerChannelTest', ~mNode, 1, 1, 'inBus', ~outBus, 'outBus', 1);
s.sendMsg('s_new', 'WFSMixerChannelTest', ~mNode2, 1, 1, 'inBus', ~outBus, 'outBus', 0);
s.sendMsg('n_set', ~mNode2, 'gate', 1);
s.sendMsg('n_set', ~mNode2, 'gate', 0);
s.sendMsg('n_set', ~mNode, 'gate', 1);
s.sendMsg('n_set', ~mNode, 'gate', 0);


SynthDef.new("testWFS", { |outBus=20, lev=0.1|
	var aSig;
	aSig = GravityGrid.ar(
		Impulse.kr(LFNoise0.kr(0.25,90,100)),
		//Impulse.kr(LFNoise0.kr([0.25,0.231],90,100)),
		//[100.2,10.5],
		100.2,
		LFNoise0.kr(10,0.8),
		LFNoise0.kr(10,0.8));
	//Out.ar(outBus, Pan2.ar(aSig) * lev);
	Out.ar(outBus, aSig * lev);
}).load(s);

SynthDef.new("WFSMixerChannelTest",
	{ |inBus=20, outBus=0, delayTime=0.01, maxDelay=1, lev=1, gain=1, gate=0|
		var aSig, aIn, aEnv;
		aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, doneAction:2);
		// gain = per-channel attenuation, lev = per-input level
		aIn = In.ar(inBus) * lev * gain;
		aSig = DelayC.ar(aIn, delayTime) * aEnv;
		Out.ar(outBus, aSig);
	}
).load(s);


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