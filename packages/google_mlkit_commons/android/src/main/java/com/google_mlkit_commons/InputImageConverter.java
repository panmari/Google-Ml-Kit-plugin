package com.google_mlkit_commons;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageWriter;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.lang.AutoCloseable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

import io.flutter.plugin.common.MethodChannel;

public class InputImageConverter implements AutoCloseable {

    ImageWriter writer;

    //Returns an [InputImage] from the image data received
    public InputImage getInputImageFromData(Map<String, Object> imageData,
            Context context,
            MethodChannel.Result result) {
        //Differentiates whether the image data is a path for a image file or contains image data in form of bytes
        String model = (String) imageData.get("type");
        InputImage inputImage;
        if (model != null && model.equals("file")) {
            try {
                inputImage = InputImage.fromFilePath(context, Uri.fromFile(new File(((String) imageData.get("path")))));
                return inputImage;
            } catch (IOException e) {
                Log.e("ImageError", "Getting Image failed");
                Log.e("ImageError", e.toString());
                result.error("InputImageConverterError", e.toString(), e);
                return null;
            }
        } else {
            if (model != null && model.equals("bytes")) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metaData = (Map<String, Object>) imageData.get("metadata");

                    assert metaData != null;
                    byte[] data = (byte[]) Objects.requireNonNull(imageData.get("bytes"));
                    int imageFormat = Integer.parseInt(Objects.requireNonNull(metaData.get("image_format")).toString());
                    int rotationDegrees = Integer.parseInt(Objects.requireNonNull(metaData.get("rotation")).toString());
                    int width = Double.valueOf(Objects.requireNonNull(metaData.get("width")).toString()).intValue();
                    int height = Double.valueOf(Objects.requireNonNull(metaData.get("height")).toString()).intValue();
                    if (imageFormat == ImageFormat.NV21 || imageFormat == ImageFormat.YV12) {
                        return InputImage.fromByteArray(
                                data,
                                width,
                                height,
                                rotationDegrees,
                                imageFormat);
                    }
                    if (imageFormat == ImageFormat.YUV_420_888) {
                        // This image format is only supported in InputImage.fromMediaImage, which requires to transform the data to the right java type.
                        // TODO: Consider reusing the same Surface across multiple calls to save on allocations.
                        writer = new ImageWriter.Builder(new Surface(new SurfaceTexture(true)))
                                .setWidthAndHeight(width, height)
                                .setImageFormat(imageFormat)
                                .build();
                        Image image = writer.dequeueInputImage();
                        if (image == null) {
                            result.error("InputImageConverterError", "failed to allocate space for input image", null);
                            return null;
                        }
                        // Deconstruct individual planes again from flattened array. 
                        Image.Plane[] planes = image.getPlanes();
                        // Y plane
                        ByteBuffer yBuffer = planes[0].getBuffer();
                        yBuffer.put(data, 0, width * height);

                        // U plane
                        ByteBuffer uBuffer = planes[1].getBuffer();
                        int uOffset = width * height;
                        uBuffer.put(data, uOffset, (width * height) / 4);

                        // V plane
                        ByteBuffer vBuffer = planes[2].getBuffer();
                        int vOffset = uOffset + (width * height) / 4;
                        vBuffer.put(data, vOffset, (width * height) / 4);
                        return InputImage.fromMediaImage(image, rotationDegrees);
                    }
                    result.error("InputImageConverterError", "ImageFormat is not supported.", null);
                    return null;
                } catch (Exception e) {
                    Log.e("ImageError", "Getting Image failed");
                    Log.e("ImageError", e.toString());
                    result.error("InputImageConverterError", e.toString(), e);
                    return null;
                }
            } else {
                result.error("InputImageConverterError", "Invalid Input Image", null);
                return null;
            }
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }

}
