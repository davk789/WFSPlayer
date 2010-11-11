WFSMarkerArea : UserView {
//	var parent, bounds;
	var coords, currentIndex=0, indexCounter=0;
	// backgroundColor is renaming this.background
	var <>markerColor, <>selectionColor, <>markerSize=5;

	*new { |view, dim|
		^super.newCopyArgs(view, dim).init_wfsmarkerarea;
	}

	init_wfsmarkerarea {
		// the coordinates need to preserve their id number
		// so, when removing elements, the remaining elements keep their identity
		//coords = Array();
		coords = [50 @ 50, 25 @ 30]; // for testing
		markerColor = Color.yellow;
		selectionColor = Color.green;
		this.background = Color.black.alpha_(0.8);

		this.setDrawFunc;
		this.refresh;
	}

	setDrawFunc {
		this.drawFunc = {
			Pen.use{
				coords.keysValuesDo{ |id, coord, ind|
					if(id == currentIndex){ 
						Pen.color = selectionColor
					}{
						Pen.color = markerColor;
					};
					
					Pen.addArc(coord, markerSize, 2pi);
					Pen.fill;
				};
			};
		};
	}

}

WFSMarkerAreaOld {
	var uView, prCoords, prCurrentIndex, updateCurrentIndex=true,
		<dimensions, markerColor, selectionColor, markerSize=5, currentMarker,
		<>mouseDownAction, <>mouseUpAction, <>mouseMoveAction, <>maxSize=8;

	*new { |view, dim|
		^super.new.init_markerarea(view, dim);
	}

	*test {
		var win;
		win = GUI.window.new("asd", Rect.new(200.rand, 200.rand, 150, 150)).front;
		^super.new.init_markerarea(win, Rect.new(0, 0, 145, 145));
	}
	init_markerarea { |view, dim|
		dimensions = dim;
		markerColor = Color.yellow;
		selectionColor = Color.green;
		prCoords = Array.new;
		mouseDownAction = { |obj,x,y,mod| };
		mouseUpAction = { |obj,x,y,mod| };
		mouseMoveAction = { |obj,x,y,mod| };
		uView = GUI.userView.new(view, dimensions)
			.background_(Color.black.alpha_(0.8))
			//.relativeOrigin_(false)
			.mouseDownAction_({ |obj,x,y,mod| 
				this.handleAddEvent(x @ y, mod);
				mouseDownAction.(obj,x,y,mod);
			})
			.mouseMoveAction_({ |obj,x,y,mod| 
				this.moveMarker(x @ y, mod); 
				mouseMoveAction.(obj,x,y,mod);
			})
			.mouseUpAction_({ |obj,x,y,mod| 
				mouseUpAction.(obj,x,y,mod);
			})
			.drawFunc_({
				Pen.use{
					Pen.color = markerColor;
					prCoords.do{ |coord,ind|
						if(ind == prCurrentIndex){ Pen.color_(selectionColor) };
						Pen.addArc(coord, markerSize, 0, 2pi);
						Pen.fill;
						if(ind == prCurrentIndex){ Pen.color_(markerColor) };
					};
				};
			});
	}
 	moveMarker { |coord,mod|
		var conf, ind;
		conf = this.getConflictPoint(coord);
		conf.isNil.if{ 
			ind = prCoords.lastIndex;
		}{ 
			ind = conf;
		};
		prCurrentIndex = ind;
		postln("prCurrentIndex = " ++ prCurrentIndex);

		if(this.countConflicts(coord) < 2){ 
			prCoords.removeAt(ind);
			this.addMarker(coord,mod); 
		};
	}
	addMarker { |coord,mod|
		if(this.getConflictPoint(coord).isNil && (prCoords.size < maxSize)){
			prCoords = prCoords.add(coord);
		};
		uView.refresh;
	}
	handleAddEvent { |coord,mod|
		if(mod == 131072){ // shift key
			this.removeMarker(coord);
		}{
			this.addMarker(coord);
		};
	}
	removeMarker { |coord|
		var rem;
		rem = this.getConflictPoint(coord);
		if(rem.notNil){ 
			prCoords.removeAt(rem) 
		};
		uView.refresh;
	}
	getConflictPoint { |coord|
		var hit=nil;
		if(prCoords.size > 0){
			prCoords.do{ |obj,ind|
					this.pointCollision(coord,obj).if{ hit = ind;};
			};
		};
		^hit;
	}
	countConflicts { |coord|
		var num=0;
		if(prCoords.size > 0){
			prCoords.do{ |obj,ind|
				this.pointCollision(coord,obj).if{ num = num + 1; };
			};
		};
		^num;	
	}
	pointCollision { |currentCoord,prevCoord|
		^(
			(
				(currentCoord.x <= (prevCoord.x + markerSize)) 
					&& 
				(currentCoord.y <= (prevCoord.y + markerSize))
			) 
			&& 
			(
				(currentCoord.x > (prevCoord.x - markerSize)) 
					&& 
				(currentCoord.y > (prevCoord.y - markerSize))
			)
		);
	}
	// getter/setter methods
	bounds_ { |val|
		uView.bounds = val;
	}
	bounds {
		^uView.bounds;
	}
	coords_ { |arr|
		prCoords = arr;
		uView.refresh;
	}
	coords {
		^prCoords;
	}
	currentIndex_ { |ind|
		prCurrentIndex = ind;
		uView.refresh;
	}
	currentIndex {
		^prCurrentIndex;
	}

}