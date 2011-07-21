WFSAbstractGUIWrapper {
	/*
		abstract class that passes all unimplemented function calls to prThis, 
		which is the wrapped gui widget implemented by the subclass. It also 
		handles edge cases where the generic wrapper implementation fails. 
	*/
	var prThis;
	*new { |par,bnd|
		^super.new.init_wfsabstractguiwrapper;
	}

	init_wfsabstractguiwrapper {
		postln(this.class.asString ++ " initialized.");
	}

	doesNotUnderstand { |selector ... args|
		var result;
		//postf("% %\n", selector, args);

		result = prThis.performList(selector, args);
		if(selector.isSetter.not){
			^result;
		};
	}

	// methods implemented by Object

	value {
		^prThis.value;
	}

}

WFSNumberBoxSpec : WFSAbstractGUIWrapper {
	/**
		a number box with a spec applied to the display.
	*/
	var <value=0; // unmapped data value of the number box
	var <action;
	var <spec;
	
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
		spec = [0, 16].asSpec;
		prThis.value = this.valueToDisplay(value);
		this.action = {};		

		this.initInputFilter;
	}

	initInputFilter {
		// all key up events push the value of the widget
		// entering non-numeric data resets the value to 0 -- best to 
		// filter non-numeric input...... later...
		prThis.keyUpAction = { |obj|
			value = this.displayToValue(obj.value);
		};
	}

	displayToValue { |disp|
		var specVal = disp % spec.maxval;
		var off = (disp / spec.maxval).trunc;
		^spec.unmap(specVal) + off
	}
	
	valueToDisplay { |val|
		var wrap = val.trunc;
		var mod = spec.map(val % 1.0);
		var off = wrap * spec.maxval;
		^mod + off
	}
	
	action_ { |act|
		action = act;
		prThis.action = { |obj|
			obj.value = this.limitDisplay(obj.value);
			value = this.displayToValue(obj.value);
			action.value(this);			
		};
	}

	valueAction_ { |val|
		// unused: implementing it in case I need valueAction_ later
		this.value = val;
		prThis.action.value(prThis);
		
	}

	value_ { |val|
		value = val;
		prThis.value = this.valueToDisplay(value);
	}

	spec_ { |newSpec|
		spec = newSpec;
		prThis.value = this.valueToDisplay(value);
	}
	
}

WFSScrollingNumberBox : WFSAbstractGUIWrapper {
	/* wrap a view redirected number box, rather than subclass. Support Cocoa this way.
	   */
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
}
