/**
 *
 */
package de.mpicbg.jug.plugins;

import ij.ImagePlus;
import ij.measure.Calibration;

import java.awt.Image;
import java.nio.ByteBuffer;

import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Plugin;

import clearvolume.network.client.main.ClearVolumeClientMain;
import clearvolume.renderer.VolumeCaptureListener;
import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;
import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;

/**
 * @author jug
 */
@Plugin( menu = { @Menu( label = "Plugins" ),
				 @Menu( label = "ClearVolume" ),
				 @Menu( label = "Start Network Client" ) }, description = "Opens network client waiting to receive data.", headless = false, type = Command.class )
public class ClearVolumeClientPlugin implements Command, VolumeCaptureListener {

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		final Image appicon = GenericClearVolumeGui.getCurrentAppIcon();

		ClearVolumeClientMain.launchClientGUI( this, appicon, false );
	}

	@Override
	public void capturedVolume(
			final ByteBuffer[] pCaptureBuffers,
			final boolean pFloatType,
			final int pBytesPerVoxel,
			final long pVolumeWidth,
			final long pVolumeHeight,
			final long pVolumeDepth,
			final double pVoxelWidth,
			final double pVoxelHeight,
			final double pVoxelDepth ) {
		final Img< FloatType > img =
				ImgLib2ClearVolume.makeImgFromBytes(
						pVolumeWidth,
						pVolumeHeight,
						pVolumeDepth,
						pBytesPerVoxel,
						pFloatType,
						pCaptureBuffers );
		final ImagePlus imagePlus = ImageJFunctions.show( img );
		final Calibration calibration = imagePlus.getCalibration();
		calibration.pixelWidth = pVoxelWidth;
		calibration.pixelHeight = pVoxelHeight;
		calibration.pixelDepth = pVoxelDepth;
		imagePlus.setCalibration( calibration );
	}
}
