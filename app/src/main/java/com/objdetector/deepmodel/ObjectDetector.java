package com.objdetector.deepmodel;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetector {
    private static final String MODEL_FILENAME = "detect2.tflite";
    private static final String LABEL_FILENAME = "labelmap.txt";
    private static final int INPUT_SIZE = 192;
    private static final int NUM_BYTES_PER_CHANNEL = 1;
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;
    private static final int NUM_DETECTIONS = 10;
    private static final String LOGGING_TAG = ObjectDetectionHelper.class.getName();

    private Interpreter tfLite;
    final int IMAGE_SIZE_X = 192;
    final int IMAGE_SIZE_Y = 192;
    final int DIM_BATCH_SIZE = 1;
    final int DIM_PIXEL_SIZE = 3;


    private ObjectDetector(final AssetManager assetManager) throws IOException {
        init(assetManager);
    }

    public static ObjectDetector create(final AssetManager assetManager) throws IOException {
        return new ObjectDetector(assetManager);
    }

    private void init(final AssetManager assetManager) throws IOException {
        tfLite = new Interpreter(loadModelFile(assetManager));
    }


    private static MappedByteBuffer loadModelFile(AssetManager assets)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(MODEL_FILENAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public List<ProcessedResult> detectObjects(final Bitmap bitmap) {
        int[] intValues = new int[IMAGE_SIZE_X * IMAGE_SIZE_Y];
        bitmap.getPixels(intValues, 0, 192, 0, 0, 192, 192);

        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * IMAGE_SIZE_X
                                * IMAGE_SIZE_Y
                                * DIM_PIXEL_SIZE
                                * NUM_BYTES_PER_CHANNEL);
        imgData.rewind();

        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE_X; ++i) {
            for (int j = 0; j < IMAGE_SIZE_Y; ++j) {
                int pixelValue = intValues[pixel++];
                imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                imgData.put((byte) (pixelValue & 0xFF));
            }
        }

        ByteBuffer[] input = { imgData };

// Output boxes.
        Map<Integer, Object> outputMap = new HashMap<>();
        int outputIndex0 = tfLite.getOutputIndex("num_detections");
        float[][][] num_detections = new float[1][100][4];
        outputMap.put(0, num_detections);

        tfLite.runForMultipleInputsOutputs(input, outputMap);

        RectF rectF = new RectF(0,0,10,10);

        final ArrayList<ProcessedResult> recognitions = new ArrayList<>(NUM_DETECTIONS);
        for (int i = 0; i < NUM_DETECTIONS; ++i) {
            final RectF detection = new RectF(0,0,10,10);
            int labelOffset = 1;
            recognitions.add(
                    new ProcessedResult(
                            i,
                            "object count",
                            (float) outputIndex0,
                            detection));
        }


        return recognitions;
    }

    public void close() {
        tfLite.close();
    }
}
