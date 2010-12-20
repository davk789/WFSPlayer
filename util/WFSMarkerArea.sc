WFSMarkerArea : JSCUserView {
	var <coords, <currentIndex=0, indexCounter=0;
	// backgroundColor is renaming this.background
	var <markerColor, <selectionColor, <>markerSize=5;
	var <>maxNumPoints=128; // mouse down may lag with too many points
	var canMove=false;

	*new { |view, dim|
		^super.new(view, dim).init_wfsmarkerarea;
	}

	*test {
		var win;
		win = Window().front;
		^super.new(win, win.view.bounds).init_wfsmarkerarea;
	}

	init_wfsmarkerarea {
		// when removing elements, the value at the index is set to nil,
		// rather than removing the value from the array outright
		// so be sure to check for nil
		coords = Array();
		markerColor = Color.yellow;
		selectionColor = Color.green;
		this.background = Color.black.alpha_(0.8);

		this.mouseDownAction = {};
		this.mouseUpAction = {};
		this.mouseMoveAction = {};

		this.setDrawFunc;
	}

	setDrawFunc {
		this.drawFunc = {
			Pen.use{
				coords.do{ |coord, ind|
					
					if(ind == currentIndex){ 
						Pen.color = selectionColor;
					}{
						Pen.color = markerColor;
					};
					
					if(coord.notNil){
						Pen.addArc(coord, markerSize, 0, 2pi);
						Pen.fill;
					};
				};
			};
		};
	}

	mouseDownAction_ { |func|
		super.mouseDownAction = { |obj,x,y,mod|
			this.handleMouseDown(x @ y, mod);
			func.value(obj,x,y,mod);
		};
	}

	mouseUpAction_ { |func| // no need to subclass this yet
		super.mouseUpAction = { |obj,x,y,mod|
			this.handleMouseUp(x @ y, mod);
			func.value(obj,x,y,mod);
		};
	}

	mouseMoveAction_ { |func|
		super.mouseMoveAction = { |obj,x,y,mod|
			this.handleMouseMove(x @ y, mod);
			func.value(obj,x,y,mod);
		};
	}

	handleMouseDown { |coord, mod|
		// check the add conditions, more to come
		var pointsNotFull, collisionPoint, numPoints;
						
		if(mod != 131072){
			pointsNotFull = true;
			collisionPoint = this.getCollisionPoint(coord);

			if(collisionPoint.isNil){
				numPoints = this.countPoints;
				pointsNotFull = numPoints < maxNumPoints;
				
				if(pointsNotFull){
					this.addMarker(coord);
				};
			}{
				currentIndex = collisionPoint;
			};
	
			canMove = pointsNotFull;
			
			this.refresh;

		}{
			canMove = false;
		};
	}

	handleMouseUp { |coord, mod|
		var collisionPoint = this.getCollisionPoint(coord);
		
		if((mod == 131072) && collisionPoint.notNil){
			this.removeMarker(collisionPoint);
		};
	}

	handleMouseMove { |coord, mod|
		if((mod != 131072) && canMove){
			this.moveMarker(coord);
		}
	}

	removeMarker { |markerIndex|
		coords[markerIndex] = nil;
		this.refresh;
	}

	addMarker { |coord|
		coords = coords.add(coord);
		currentIndex = coords.lastIndex;
		this.refresh;
	}

	moveMarker { |coord|
		coords[currentIndex] = coord;
		this.refresh;
	}
	
	countPoints {
		var ret=0;
		coords.do{ |obj,ind| 
			if(obj.notNil){
				ret = ret + 1;
			};
		};
		
		^ret;
	}

	getCollisionPoint { |coord|
		var diff;
		coords.do{ |obj,ind|
			if(obj.notNil){
				diff = abs(obj - coord);
				if((diff.x < markerSize) && (diff.y < markerSize)){
					^ind;	
				};
			};
		};

		^nil;
	}

	coords_ { |newCoords|
		coords = newCoords;
		this.refresh;
	}
	
	markerColor_ { |color|
		markerColor = color;
		this.refresh;
	}
	
	selectionColor_ { |color|
		selectionColor = color;
		this.refresh;
	}
}
