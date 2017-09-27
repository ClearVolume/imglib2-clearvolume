/**
 *
 */
package de.mpicbg.jug.plugins;

import java.awt.Image;
import java.nio.ByteBuffer;

import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Plugin;

import clearvolume.network.client.main.ClearVolumeClientMain;
import clearvolume.renderer.listeners.VolumeCaptureListener;
import coremem.enums.NativeTypeEnum;
import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;
import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;
import ij.ImagePlus;
import ij.measure.Calibration;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

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
//		final Image appicon = GenericClearVolumeGui.getCurrentAppIcon();
//		ClearVolumeClientMain.launchClientGUI( this, appicon, false );
		
		System.setProperty("newt.window.icons", "null,null");
		ClearVolumeClientMain.launchClientGUI( this, null, false );
	}

	/**
	 * @see clearvolume.renderer.listeners.VolumeCaptureListener#capturedVolume(java.nio.ByteBuffer,
	 *      coremem.types.NativeTypeEnum, long, long, long, double, double,
	 *      double)
	 */
	@Override
	public void capturedVolume(
			final ByteBuffer pCaptureBuffer,
			final NativeTypeEnum pNativeTypeEnum,
			final long pVolumeWidth,
			final long pVolumeHeight,
			final long pVolumeDepth,
			final double pVoxelWidth,
			final double pVoxelHeight,
			final double pVoxelDepth ) 
	{
		final Img< FloatType > img =
				ImgLib2ClearVolume.makeImgFromBytes(
						pVolumeWidth,
						pVolumeHeight,
						pVolumeDepth,
						pNativeTypeEnum,
						new ByteBuffer[]{pCaptureBuffer} );
		final ImagePlus imagePlus = ImageJFunctions.show( img );
		final Calibration calibration = imagePlus.getCalibration();
		calibration.pixelWidth = pVoxelWidth;
		calibration.pixelHeight = pVoxelHeight;
		calibration.pixelDepth = pVoxelDepth;
		imagePlus.setCalibration( calibration );
	}
}
