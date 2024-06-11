mSLA 3D-printer file processing library
======

This library was inspired by UVTools (chaps, you did an enormous amount of work reversing all that file formats!).
Initially the idea was to write a tool that can display one image on mSLA printer's screen so
that I could get PCB done without any photo-printing.

The development is still in progress, I haven't tested files that this library can
generate, although tried with Anycubic Photon 6K that I have.

Supported mSLA file formats:
- Photon workshop (PW0 codec supported, PWS is not)
- Creality (CXDLP)
- Chitubox (both encrypted and plain)
- Elegoo GOO files

Pre-configured devices with defaults:
- Anycubic Photon M3 Max
- Anycubic Photon Mono X 6K
- Anycubic Photon Mono 4K
- Anycubic Photon Mono X
- Anycubic Photon M3
- Creality HALOT-ONE PLUS
- Creality HALOT-ONE PRO
- Creality HALOT-ONE
- Creality HALOT-RAY
- Elegoo Mars 4 Max
- Elegoo SATURN
- Elegoo JUPITER

## API

API is fairly straightforward, although it can be tricky in terms of setting
various options for files. The main idea here is to provide versatile tool that
allows setting options in machine-agnostic way.

### Load a file

```java
var file = FileFactory.instance.load('/path_to_file/file_name.ctb');
```

### Create a file

```java
var machine = "CREALITY HALOT-ONE PLUS";
var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
    .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
var file = FileFactory.instance.create(machine);
```

### Set file options
```java
void setOptions(MSLAFile file, MSLAFileDefaults defaults) {
    var options = new FileOptionMapper(file, defaults);
    options.set(MSLAOptionName.BottomLayersExposureTime, "12");
    options.set(MSLAOptionName.LayerHeight, "0.1");
}
```

### Extract layer data

The following code will save 2 images of layers 1 and 10 to current folder.
Decoding is being done asynchronously, so it has to wait while working.

```java
void readFile() {
    var temp_dir = ".";
    var file = FileFactory.instance.load('/path_to_file/file_name.ctb');
    var writer = new ImageWriter(file, temp_dir, "extracted_", "png");
    file.readLayer(writer, 1);
    file.readLayer(writer, 10);
    while (file.getDecodersPool().isDecoding()) ; // Wait while decoding-writing is done
}
```

Image writer can be assigned with a callback:

```java
void decodeWithCallback() {
    var layerPixels = new int[3];
    var layerFiles = new String[3];
    var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
        layerPixels[layerNumber] = pixels;
        layerFiles[layerNumber] = fileName;
    });
    for (var i = 0; i < 3; i++) file.readLayer(writer, i);
    while (file.getDecodersPool().isDecoding());
    System.out.println("Layer 0 is in a file" + layerFiles[0] + 
            " and has " + layerPixels[0] + 
            " non-black pixels");
}
```

### Add layers from images

Layers are added in the order of addLayer() calls, but encoded asynchronously.
Hence, have to wait until encoder is done its work.

```java
void addLayers() {
    var pngFileLayers = new String[]{"Layer_1.png", "Layer_2.png"};
    for (var pngFile : pngFileLayers) {
        try {
            file.addLayer(new ImageReader(file, pngFile), null);
        } catch (IOException e) {
            throw new MSLAException("Can't read layer image", e);
        }
    }
    while (file.getEncodersPool().isEncoding());
}
```

### Extracting previews

Some files formats support only one preview and some of them have more, 
so getPreview() receives preview index.

```java
void extractPreview() {
    var file = FileFactory.instance.load('/path_to_file/file_name.ctb');
    var image = file.getPreview(0).getImage();
    ImageIO.write(image, "png", new File("path_to_preview/preview.png"));
}
```

This example allows to get large preview. Depending on a mSLA file format
that is going to be the largest preview image available.

```java
var image = file.getLargePreview().getImage();
```

## DISCLAIMER

This software is still in development, so you can use it on you own risk.