WFSVTransportView : JSCVLayoutView {
	var playButton, forwardButton, reverseButton, pauseButton, stopButton, recordButton;
	var <>playAction, <>forwardAction, <>reverseAction, <>pauseAction, <>stopAction, <>recordAction;
	var buttonHeight;

	/**
		Half-assed container for transport buttons. Maybe add full support later, right
		now the class only implements exactly what I am using.

		This class implements 4 transport buttons: Play Pause Stop and Record. The buttons
		affect each others' values in the espected way. The buttons call their respective
		actions and pass themselves to the functions.
	*/
	
	*new { |par,dim|
		^super.new(par,dim).init_wfsvtransportview;
	}

	init_wfsvtransportview {
		buttonHeight = (this.bounds.height / 6) - (this.bounds.height * 0.05);
		playAction = {};
		forwardAction = {};
		reverseAction = {};
		pauseAction = {};
		stopAction = {};
		recordAction = {};
		
		this.makeGUI;
		
		postln(this.class.asString ++ " initialized");
	}

	makeGUI {
		playButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([[">", Color.white, Color.black], [">", Color.black, Color.white]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				playAction.value(obj);
			});
		
		forwardButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([[">>", Color.white, Color.black]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				forwardAction.value(obj);
			});

		reverseButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([["<<", Color.white, Color.black]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				reverseAction.value(obj);
			});

		pauseButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([["||", Color.yellow, Color.black], ["||", Color.black, Color.yellow]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				pauseAction.value(obj);
			});

		stopButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([["[]", Color.white, Color.black]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				playButton.valueAction = 0;
				recordButton.valueAction = 0;
				stopAction.value(obj);
			});
		
		recordButton = Button(this, Rect(0, 0, 0, buttonHeight))
		    .states_([["o", Color.red, Color.black], ["o", Color.black, Color.red]])
		    .font_(Font("Arial Black", 12))
		    .action_({ |obj|
				recordAction.value(obj);
			});
		
	}

	enabled_ { |en|
		super.enabled = en;
		playButton.enabled = en;
		forwardButton.enabled = en;
		reverseButton.enabled = en;
		pauseButton.enabled = en;
		recordButton.enabled = en;
		stopButton.enabled = en;
	}

	action_ {
		error("this class does not implement actions. Use playAction, recordAction, stopAction, pauseAction instead.")
	}

	action {
		error("this class does not implement actions. Use playAction, recordAction, stopAction, pauseAction instead.")
	}

}