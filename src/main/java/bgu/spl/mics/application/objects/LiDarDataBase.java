package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for
 * tracked objects.
 */
public class LiDarDataBase {
    public static List<StampedCloudPoints> stampedCloudPoints = null;
    public static int maxTime;

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

    private static class SingletonLiDarDataBase {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    public static LiDarDataBase getInstance(String filePath) {
        if (stampedCloudPoints == null) {
            JsonParser parser = new JsonParser();
            stampedCloudPoints = parser.getLidarData(filePath);
        }
        
        maxTime = stampedCloudPoints.get(stampedCloudPoints.size() - 1).getTime();
        return SingletonLiDarDataBase.INSTANCE;
    }

    public List<StampedCloudPoints> getStampedCloudPoints() {
        return stampedCloudPoints;
    }

    public int getListSize() {
        return stampedCloudPoints.size();
    }

    public static void setDataForUniTests(List<StampedCloudPoints> newStampedCloudPoints) {
        stampedCloudPoints = newStampedCloudPoints;
    }
}