package de.mpicbg.jug;

import java.io.File;
import java.io.IOException;

import net.imagej.ImageJ;


/**
 *
 */

/**
 * @author jug
 */
public class testImageJPlugin {

	public static void main( final String[] args ) {
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/droso.tif";
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic.tif";
//		final String fname = "/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic_labels.tif";
		final String fname =
				"/Users/jug/Desktop/ClearVolumeDatasetSamples/synthetic_twoChannel.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/Flybrain_2ch_12_smallSize.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/mitosis5d.tif";
//		final String fname =
//				"/Users/jug/Desktop/ClearVolumeDatasetSamples/norden5d.tif";

		final File file = new File( fname );

		final ImageJ ij = new ImageJ();
		try {
			Object img = null;
			if ( file.exists() && file.canRead() ) {
				img = ij.io().open( fname );
			}

			ij.ui().showUI();

			if ( img != null ) {
				ij.command().run( de.mpicbg.jug.plugins.ClearVolumePlugin.class, true, "dataset", img );
			}
		} catch ( final IOException e ) {
			e.printStackTrace();
		}
	}
}
