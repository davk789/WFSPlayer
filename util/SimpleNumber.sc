+ SimpleNumber {
	frand { |precision=100|
		var upscale, downscale, ratio;
		ratio = precision / this.value;
		upscale = this.value * ratio;
		downscale = upscale.rand / ratio;
		^downscale;
	}
	
	increment { |amt=1|
		^this.value + amt;
	}
	
	decrement { |amt=1|
		^this.value - amt;
	}
	
	toBool {
		if(this.value.trunc < 1){
			^false;
		}{
			^true;
		};
	}
	
}

+ True {
	toInt {
		^ 1;
	}
}

+ False {
	toInt {
		^0;
	}
}