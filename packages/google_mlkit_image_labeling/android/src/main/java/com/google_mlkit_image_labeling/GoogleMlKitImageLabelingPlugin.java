package com.google_mlkit_image_labeling;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import com.google_mlkit_commons.Messages.ImageLabelDetectorApi;

public class GoogleMlKitImageLabelingPlugin implements FlutterPlugin {

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        ImageLabelDetector api = new ImageLabelDetector(flutterPluginBinding.getApplicationContext());
        
        ImageLabelDetectorApi.setUp(flutterPluginBinding.getBinaryMessenger(), api);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        // TODO(panmari): Some teardown?
    }
}
