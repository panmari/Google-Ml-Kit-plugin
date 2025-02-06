import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/src/messages.g.dart',
  dartOptions: DartOptions(),
  gobjectOptions: GObjectOptions(),
  javaOut: 'android/src/main/java/com/google_mlkit_commons/Messages.java',
  javaOptions: JavaOptions(
    package: 'com.google_mlkit_commons',
  ),
  objcHeaderOut: 'ios/Runner/messages.g.h',
  objcSourceOut: 'ios/Runner/messages.g.m',
  // Set this to a unique prefix for your plugin or application, per Objective-C naming conventions.
  objcOptions: ObjcOptions(prefix: 'PGN'),
))

/// Data of image required when creating image from bytes.
class InputImageMetadata {
  int width;
  int height;

  /// Image rotation in degree.
  ///
  /// Not used on iOS.
  final int rotation;

  /// Format of the input image.
  ///
  /// Android supports
  /// - [InputImageFormat.nv21]
  /// - [InputImageFormat.yuv_420_888]
  /// - [InputImageFormat.yv12]
  /// as described in [here](https://developers.google.com/android/reference/com/google/mlkit/vision/common/InputImage.ImageFormat).
  ///
  /// iOS supports
  /// - [InputImageFormat.yuv420]
  /// - [InputImageFormat.bgra8888]
  final int format;

  /// The row stride for color plane, in bytes.
  ///
  /// Not used on Android.
  final int bytesPerRow;

  /// Constructor to create an instance of [InputImageMetadata].
  InputImageMetadata({
    required this.width,
    required this.height,
    required this.rotation,
    required this.format,
    required this.bytesPerRow,
  });
}

enum InputImageType {
  file,
  bytes,
}

class InputImageMessage {
  /// The type of image.
  final InputImageType type;

  /// The file path to the image.
  String? filePath;

  /// The bytes of the image.
  Uint8List? bytes;

  /// The image data when creating an image of type = [InputImageType.bytes].
  InputImageMetadata? metadata;

  InputImageMessage({required this.type});
}

enum ImageLabelerType {
  base,
  local,
  remote,
}

/// Base options for [ImageLabeler].
class ImageLabelerOptionsMessage {
  /// The confidence threshold for labels returned by the image labeler.
  /// Labels returned by the image labeler will have a confidence level higher or equal to the given threshold.
  /// The value must be a floating-point value in the range [0, 1].
  /// Default value is set 0.5.
  final double confidenceThreshold;

  /// Indicates that it uses Google's base model to process images.
  final ImageLabelerType type;

  /// Constructor to create an instance of [ImageLabelerOptionsMessage].
  ImageLabelerOptionsMessage({this.confidenceThreshold = 0.5, this.type = ImageLabelerType.base});
}

/// Represents a label detected in an image.
class ImageLabelMessage {
  /// The confidence(probability) given to label that was identified in image.
  final double confidence;

  /// Label or title given for detected entity in image.
  final String label;

  /// Index of label according to google's label map: https://developers.google.com/ml-kit/vision/image-labeling/label-map
  final int index;

  /// Constructor to create an instance of [ImageLabel].
  ImageLabelMessage(
      {required this.confidence, required this.label, required this.index});
}

@HostApi()
abstract class ImageLabelDetectorApi {
  
  @async
  List<ImageLabelMessage> handleDetection(InputImageMessage inputImage);

  void create(ImageLabelerOptionsMessage options);
  void manageModel(String modelName);
  void closeDetector(String id);
}
