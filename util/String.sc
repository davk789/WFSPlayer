+ Object { 
	asInfString { // non-truncated string conversion
		var string;
		_ObjectString
		string = String.streamContentsLimit({ arg stream; this.printOn(stream); }, inf);
		^string
	}
}
