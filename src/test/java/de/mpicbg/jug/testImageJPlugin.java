package de.mpicbg.jug;

import java.io.File;
import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;

/**
 *
 */

/**
 * @author jug
 */
public class testImageJPlugin {

	public static void main( final String[] args ) {
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/XY.tif";		// OK
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/XYC.tif";	// OK
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/XYZ.tif";	// OK
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/XYCZ.tif";	// OK
		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/XYCZT.tif";

//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/droso.tif";
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic.tif";
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic_labels.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic_twoChannel.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/Flybrain_2ch_12_smallSize.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/mitosis4d.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/mitosis5d.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/norden5d.tif";

		final File file = new File( fname );

		final ImageJ ij = new ImageJ();
		try {
			Dataset ds = null;
			DatasetView dsv = null;
			if ( file.exists() && file.canRead() ) {
				ds = ij.scifio().datasetIO().open( fname );
//				dsv = ( DatasetView ) ij.imageDisplay().createDataView( ds );
//				dsv.rebuild();
				final ImageDisplay display = ( ImageDisplay ) ij.display().createDisplay( ds );
				dsv = ij.imageDisplay().getActiveDatasetView( display );

			}

			ij.ui().showUI();

			if ( ds != null ) {
				ij.command().run(
						de.mpicbg.jug.plugins.ClearVolumePlugin.class,
						true,
						"datasetView",
						dsv );
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}
