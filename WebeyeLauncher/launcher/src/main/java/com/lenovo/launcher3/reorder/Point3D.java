package com.lenovo.launcher3.reorder;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

public class Point3D extends Point {
	public int screen;
	
	public Point3D() {}
	
	public Point3D(int screen, int x, int y) {
		super(x, y);
		this.screen = screen;
	}
	
	public Point3D(Point3D src) {
		super(src);
		this.screen = src.screen;
    }

    /**
     * Set the point's x and y coordinates
     */
    public void set(int screen, int x, int y) {
    	this.screen = screen;
    	super.set(x, y);
    }

    /**
     * Negate the point's coordinates
     */
    public final void negate3D() {
        super.negate();
        screen = -screen;
    }

    /**
     * Offset the point's coordinates by dx, dy
     */
    public final void offset3D(int dScreen, int dx, int dy) {
        super.offset(dx, dy);
        screen += dScreen;
    }

    /**
     * Returns true if the point's coordinates equal (x,y)
     */
    public final boolean equals3D(int screen, int x, int y) {
        return super.equals(x, y) && this.screen == screen;
    }

    @Override 
    public boolean equals(Object o) {
        if (o instanceof Point3D) {
        	Point3D p = (Point3D) o;
            return this.screen == p.screen && this.x == p.x && this.y == p.y;
        }
        return false;
    }

    @Override 
    public int hashCode() {
        return x * 32713 + y + screen * 373;
    }

    @Override public String toString() {
        return "Point3D(" + screen + ", " + x + ", " + y+ ")";
    }

    /**
     * Write this point to the specified parcel. To restore a point from
     * a parcel, use readFromParcel()
     * @param out The parcel to write the point's coordinates into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(screen);
    }

    public static final Parcelable.Creator<Point3D> CREATOR = new Parcelable.Creator<Point3D>() {
        /**
         * Return a new point from the data in the specified parcel.
         */
        public Point3D createFromParcel(Parcel in) {
        	Point3D r = new Point3D();
            r.readFromParcel(in);
            return r;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        public Point3D[] newArray(int size) {
            return new Point3D[size];
        }
    };

    /**
     * Set the point's coordinates from the data stored in the specified
     * parcel. To write a point to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the point's coordinates from
     */
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        screen = in.readInt();
    }
}
