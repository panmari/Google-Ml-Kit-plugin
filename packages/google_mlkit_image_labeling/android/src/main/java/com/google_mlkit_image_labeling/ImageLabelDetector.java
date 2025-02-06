package com.google_mlkit_image_labeling;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.mlkit.common.model.CustomRemoteModel;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.linkfirebase.FirebaseModelSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google_mlkit_commons.GenericModelManager;
import com.google_mlkit_commons.InputImageConverter;
import com.google_mlkit_commons.Messages.ImageLabelDetectorApi;
import com.google_mlkit_commons.Messages.InputImageType;
import com.google_mlkit_commons.Messages.ImageLabelerOptionsMessage;
import com.google_mlkit_commons.Messages.ImageLabelerType;
import com.google_mlkit_commons.Messages.ImageLabelMessage;
import com.google_mlkit_commons.Messages.InputImageMessage;
import com.google_mlkit_commons.Messages.Result;
import com.google_mlkit_commons.Messages.FlutterError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageLabelDetector implements ImageLabelDetectorApi {

    private final Context context;
    private ImageLabeler imageLabeler;

    public ImageLabelDetector(Context context) {
        this.context = context;
    }

    public void create(ImageLabelerOptionsMessage options) {
        if (options.getType() == ImageLabelerType.BASE) {
            ImageLabelerOptions labelerOptions = getDefaultOptions(options);
            this.imageLabeler = ImageLabeling.getClient(labelerOptions);
        }
        throw new FlutterError("Not yet implemented", "", "");
    }

    public void handleDetection(InputImageMessage inputImageMessage, Result<List<ImageLabelMessage>> result) {
        InputImage inputImage = InputImageConverter.getInputImageFromData(inputImageMessage, this.context);
        if (inputImage == null) {
            throw new FlutterError("Input image is null", "", "");
        }

        if (this.imageLabeler == null) {
            throw new FlutterError("Not yet initialize, call `create` before using", "", "");
        }

        imageLabeler.process(inputImage)
                .addOnSuccessListener(imageLabels -> {
                    List<ImageLabelMessage> labels = new ArrayList<>(imageLabels.size());
                    for (ImageLabel label : imageLabels) {
                        labels.add(new ImageLabelMessage.Builder().setConfidence((double) label.getConfidence()).setLabel(label.getText()).setIndex((long) label.getIndex()).build());
                    }
                    result.success(labels);
                })
                .addOnFailureListener(e -> result.error(e));
    }

    //Labeler options that are provided to default image labeler(uses inbuilt model).
    private ImageLabelerOptions getDefaultOptions(ImageLabelerOptionsMessage labelerOptions) {
        return new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(labelerOptions.getConfidenceThreshold().floatValue())
                .build();
    }

    // //Options for labeler to work with custom model.
    // private CustomImageLabelerOptions getLocalOptions(Map<String, Object> labelerOptions) {
    //     float confidenceThreshold = (float) (double) labelerOptions.get("confidenceThreshold");
    //     int maxCount = (int) labelerOptions.get("maxCount");
    //     String path = (String) labelerOptions.get("path");
    //     LocalModel localModel = new LocalModel.Builder()
    //             .setAbsoluteFilePath(path)
    //             .build();
    //     return new CustomImageLabelerOptions.Builder(localModel)
    //             .setConfidenceThreshold(confidenceThreshold)
    //             .setMaxResultCount(maxCount)
    //             .build();
    // }
    // //Options for labeler to work with custom model.
    // private CustomImageLabelerOptions getRemoteOptions(Map<String, Object> labelerOptions) {
    //     float confidenceThreshold = (float) (double) labelerOptions.get("confidenceThreshold");
    //     int maxCount = (int) labelerOptions.get("maxCount");
    //     String name = (String) labelerOptions.get("modelName");
    //     FirebaseModelSource firebaseModelSource = new FirebaseModelSource.Builder(name).build();
    //     CustomRemoteModel remoteModel = new CustomRemoteModel.Builder(firebaseModelSource).build();
    //     if (!genericModelManager.isModelDownloaded(remoteModel)) {
    //         return null;
    //     }
    //     return new CustomImageLabelerOptions.Builder(remoteModel)
    //             .setConfidenceThreshold(confidenceThreshold)
    //             .setMaxResultCount(maxCount)
    //             .build();
    // }
    // public void closeDetector(String id) {
    //     ImageLabeler imageLabeler = instances.get(id);
    //     if (imageLabeler == null) {
    //         return;
    //     }
    //     imageLabeler.close();
    //     instances.remove(id);
    // }
    public void manageModel(String modelName) {
        FirebaseModelSource firebaseModelSource = new FirebaseModelSource.Builder(modelName)
                .build();
        CustomRemoteModel model = new CustomRemoteModel.Builder(firebaseModelSource)
                .build();
        // TODO(panmari): Support more arguments and pipe further here.
        // genericModelManager.manageModel(model);
    }

    public void closeDetector(String id) {
        // TODO?
    }
}
