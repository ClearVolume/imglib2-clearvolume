/**
 *
 */
package de.mpicbg.jug.plugins;

import java.nio.ByteBuffer;

import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Plugin;

import clearvolume.network.client.main.ClearVolumeClientMain;
import clearvolume.renderer.VolumeCaptureListener;

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
		ClearVolumeClientMain.launchClientGUI( this );
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
		System.out.format( "Captured %d volume %s bpv=%d (%d, %d, %d) (%g, %g, %g) %s\n",
				pCaptureBuffers.length,
				pFloatType ? "float" : "int",
				pBytesPerVoxel,
				pVolumeWidth,
				pVolumeHeight,
				pVolumeDepth,
				pVoxelWidth,
				pVoxelHeight,
				pVoxelDepth,
				pCaptureBuffers[ 0 ].toString() );
	}
}
