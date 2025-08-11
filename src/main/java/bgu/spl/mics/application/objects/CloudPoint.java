package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {

    double x;
    double y;
    
    public CloudPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){ return this.x; }
    public double getY(){ return this.y; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CloudPoint that = (CloudPoint) obj;
        return x == that.x && y == that.y;
    }
}
