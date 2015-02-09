# imglib2-clearvolume
This repository contains 
* all functionalities to bridge from loaded imglib2 image containers to ClearVolume.
* a ImageJ2/Fiji plugin that embedds [ClearVolume](https://bitbucket.org/clearvolume/clearvolume/wiki/Home) in a usable way.
* the uer interface can also be used in other contexts, e.g. as a [viewer plugin in KNIME](http://tech.knime.org/book/clearvolume).

Since ClearVolume can, to date, only show images stored in native `byte` arrays we need to translate imglib2 image container to native byte type images. While this is ok for 8bit images, 16bit images are represented as arrays of type `short` in imglib.
Here you find two things that enable you to display every `RealType` image container coming from imglib2 into ClearVolume:
* `ClearVolumeUnsignedShortType` -- a alternative 16 bit `short` type image type implementation that uses a native `byte` array to store voxel intensities. An image that is loaded into any imglib2 container using a image factory with this new type can directly be shown in ClearVolume using static methods in class `ClearVolume`.
* For any other imglib2 container we offer the converter `RealClearVolumeUnsignedShortConverter`. Feel free to use this converter just as any other imglib2 converter, but we do also provide static convenience methods in class `ClearVolume`.

Sneak peak at the GUI:
![Fiji plugin screenshot](images/fijiplugin.png)
