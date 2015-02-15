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

import com.apple.eawt.Application;

import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;

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
		final String os = System.getProperty( "os.name" ).toLowerCase();
		Image icon = null;
		if ( os.indexOf( "mac" ) >= 0 ) {
			icon = Application.getApplication().getDockIconImage();
		} else if ( os.indexOf( "win" ) >= 0 ) {
//			not yet clear
			icon = null;
		} else {
//			not yet clear
			icon = null;
		}
		final Image finalicon = icon;

		ClearVolumeClientMain.launchClientGUI( this, false );

		javax.swing.SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				if ( os.indexOf( "mac" ) >= 0 ) {
					Application.getApplication().setDockIconImage( finalicon );
				} else if ( os.indexOf( "win" ) >= 0 ) {
//					not yet clear
				} else {
//					not yet clear
				}
			}

		} );
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
