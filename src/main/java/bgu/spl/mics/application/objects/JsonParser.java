package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonParser {
    Gson gson = new Gson();

    public JsonParser() {

    }

    public CameraData getCameraData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type cameraDataType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();

            Map<String, List<StampedDetectedObjects>> cameras = gson.fromJson(reader, cameraDataType);
            CameraData cameraData = new CameraData(cameras);

            return cameraData;
        } catch (IOException e) {

        }

        return null;
    }

    public List<StampedCloudPoints> getLidarData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type stampedObjectListType = new TypeToken<List<StampedCloudPoints>>() {
            }.getType();
            List<StampedCloudPoints> stampedCloudPoints = gson.fromJson(reader, stampedObjectListType);

            return stampedCloudPoints;

        } catch (IOException e) {

        }

        return null;
    }

    public List<Pose> getPoseData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type PoseListType = new TypeToken<List<Pose>>() {
            }.getType();

            List<Pose> poseList = gson.fromJson(reader, PoseListType);
            return poseList;

        } catch (IOException e) {
            
        }

        return null;
    }

    public Configuration getConfiguration(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type confType = new TypeToken<Configuration>() {
            }.getType();
            Configuration configFile = gson.fromJson(reader, confType);
            return configFile;
        } catch (IOException e) {

        }

        return null;
    }
}
