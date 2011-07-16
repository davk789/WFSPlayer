WFSNumberBoxSpec {
	/**
		a number box with a value scaled 0..1, with a display set to a spec. setting and
		retrieving values will accept and return unit values only. use a special call
		to get the mapped display value.
	*/
	var <>prThis;
	var <>displaySpec;
	
	*new { |par,bnd|
		^super.new.init_wfsscalednumberbox(par, bnd);
	}

	*test {
		var win;
		win = Window("testing " ++ this.class.asString, Rect(200, 200, 400, 100)).front;
		^super.new.init_wfsscalednumberbox(win, win.view.bounds);
	}

	init_wfsscalednumberbox { |par, bnd|
		prThis = NumberBox(par, bnd);
		displaySpec = [0, 16].asSpec;
		postln(this.class.asString ++ " initalized");
	}
	
	range_ { |rng|
		displaySpec.maxval = rng + displaySpec.minval;
	}

	// write wrapper functions ....
	// it would be nice to subclass Object:doesNotUnderstand, but I 
	// seem to be missing something

	action_ { |act|
		prThis.action = { |obj|
			var tmpObj = obj.copy;
			tmpObj.value = 
		};
	}
}

WFSScrollingNumberBox {
	/* wrap a view redirected number box, rather than subclass. Support Cocoa this way.
	   */
	var prThis; // the wrapped number box
	var lastCoord=0;
	// WARNING: this class implements the same function for action and mouseMoveAction
	var subclassAction, subclassMouseUpAction;
	var <>minVal, <>maxVal;

	*new { |par,bnd|
		^super.new.init_wfsscrollingnumberbox(par,bnd);
	}

	*test {
		var win;
		win = Window().front;
		^super.new.init_wfsscrollingnumberbox(win, Rect(0, 0, 75, 25));
	}
	
	init_wfsscrollingnumberbox { |par,bnd|
		prThis = NumberBox(par,bnd);
		prThis.value = 0;
		prThis.action = {};	
	}
	
/*	mouseDownAction_ { |func|
		
		}*/

	mouseUpAction {
		^subclassMouseUpAction;
	}

	mouseUpAction_ { |func|
		super.action = { |obj,x,y,mod|
			this.checkRange;
			subclassMouseUpAction.value(obj,x,y,mod);
		};
	}

	mouseMoveAction_ {
		error("mouseMoveAction_ is disabled in this class! Please set action_ instead.");
	}

	mouseMoveAction {
		error("mouseMoveAction is disabled in this class! please use action instead.");
	}

	action_ { |func|
		subclassAction = func;
		prThis.action = { |obj,x,y,mod|
			this.checkRange;
			subclassAction.value(obj);
		};

		prThis.mouseMoveAction = { |obj,x,y,mod|
			this.checkRange;
			this.handleMouseMove(y, mod);
			subclassAction.value(obj, x, y, mod);
		};
	}

	valueAction_ { |val|
		prThis.valueAction = val;
	}

	checkRange {
		if(minVal.notNil){
			if(prThis.value < minVal){ prThis.value = minVal; };
		};

		if(maxVal.notNil){
			if(prThis.value > maxVal){ prThis.value = maxVal; };
		};
	}
	
	handleMouseMove { |loc, mod|
		// the disct now contains modifiers for both GUI schemes
		var incrementAmount	= Dictionary[
			0 -> 1,        // default SwingOSC
			524288 -> 0.1, // option  SwingOSC
			131072 -> 100, // shift   SwingOSC
			256 -> 1,      // default CocoaGUI 
			524576 -> 0.1, // option  CocoaGUI
			131330 -> 100, // shift   CocoaGUI
		];
		
		if(lastCoord < loc){
			prThis.value = prThis.value - incrementAmount[mod];
		};
		
		if(lastCoord > loc){
			prThis.value = prThis.value + incrementAmount[mod];
		};
		
		lastCoord = loc;
	}
	
	value_ { |val|
		prThis.value = val;
	}
	
	value {
		^prThis.value;
	}
	
	background_ { |color|
		prThis.background = color;
	}
	
	background {
		^prThis.background;
	}
	
	enabled_ { |choice|
		prThis.enabled = choice;
	}
	
	enabled {
		prThis.enabled;
	}

}
