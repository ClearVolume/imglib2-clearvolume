/**
 *
 */
package de.mpicbg.jug.plugins;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import clearvolume.renderer.ClearVolumeRendererInterface;
import de.mpicbg.jug.clearvolume.ClearVolume;

/**
 * @author jug
 */
@Plugin( menu = { @Menu( label = "Plugins" ), @Menu( label = "ClearVolume" ) }, description = "Opens Stack in ClearVolume.", headless = false, type = Command.class )
public class ClearVolumePlugin< T extends RealType< T > & NativeType< T >> implements Command {

	@Parameter( label = "3D ImgPlus to be shown." )
	private Dataset dataset;
	private ImgPlus< T > imgPlus;

	@Parameter( label = "Window width", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Width of the frame to be opened." )
	private int windowWidth;
	@Parameter( label = "Window height", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Height of the frame to be opened." )
	private int windowHeight;

	@Parameter( label = "Texture width", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Width of the texture to be rendered." )
	private int textureWidth;
	@Parameter( label = "Texture height", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Height of the texture to be rendered." )
	private int textureHeight;

//	@Parameter( label = "Texture width", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Width of the texture to be rendered." )
	private double minIntensity;
//	@Parameter( label = "Texture height", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Height of the texture to be rendered." )
	private double maxIntensity;

//	@Parameter( label = "VoxelSize.X", min = "0.01", max = "100", stepSize = "0.25", columns = 5, description = "Width of a voxel." )
	private double voxelSizeX;
//	@Parameter( label = "VoxelSize.Y", min = "0.01", max = "100", stepSize = "0.25", columns = 5, description = "Height of a voxel." )
	private double voxelSizeY;
//	@Parameter( label = "VoxelSize.Z", min = "0.01", max = "100", stepSize = "0.25", columns = 5, description = "Depth of a voxel." )
	private double voxelSizeZ;

	private ClearVolumeThread cvThread;

	private class ClearVolumeThread implements Runnable {

		private final boolean runInNativeFrame = true;
		private ClearVolumeRendererInterface cv;

		@Override
		public void run() {
			imgPlus = ( ImgPlus< T > ) dataset.getImgPlus();

			setDefaultValues();
			setMetadataValues();

			if ( runInNativeFrame ) {

				cv = ClearVolume.initRealImg( imgPlus, "ClearVolume TableCellView", windowWidth, windowHeight, textureWidth, textureHeight, false, minIntensity, maxIntensity );
				cv.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
				cv.requestDisplay();

			} else {

				cv = ClearVolume.initRealImg( imgPlus, "ClearVolume TableCellView", windowWidth, windowHeight, textureWidth, textureHeight, true, minIntensity, maxIntensity );
				cv.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
				cv.requestDisplay();

				final JFrame frame = new JFrame( "ClearVolume" );
				frame.setLayout( new BorderLayout() );
				final Container container = new Container();
				container.setLayout( new BorderLayout() );
				container.add( cv.getNewtCanvasAWT(), BorderLayout.CENTER );
				frame.setSize( new Dimension( windowWidth, windowHeight ) );
				frame.add( container );
				SwingUtilities.invokeLater( new Runnable() {

					@Override
					public void run() {
						frame.setVisible( true );
					}

				} );

			}

		}

		public void dispose() {
			if ( cv != null ) {
				cv.setVisible( false );
				cv.close();
			}
		}

		public void resetView() {
			setDefaultValues();
			setMetadataValues();
			cv.resetRotationTranslation();
			cv.resetBrightnessAndGammaAndTransferFunctionRanges();
			cv.setVisible( false );
			cv.setVisible( true );
		}

	}

	public ClearVolumePlugin() {
		setDefaultValues();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		cvThread = new ClearVolumeThread();
		cvThread.run();
	}

	private void setDefaultValues() {
		this.windowWidth = 512;
		this.windowHeight = 512;
		this.textureWidth = 1024;
		this.textureHeight = 1024;
		this.minIntensity = 0.;
		this.maxIntensity = 255;
		this.voxelSizeX = 1.;
		this.voxelSizeY = 1.;
		this.voxelSizeZ = 1.;
	}

	/**
	 * Uses the metadata in the ImgPlus to set voxel dimension and intensity
	 * range.
	 */
	public void setMetadataValues() {
		if ( imgPlus != null ) {
			this.voxelSizeX = imgPlus.averageScale( 0 );
			this.voxelSizeY = imgPlus.averageScale( 1 );
			this.voxelSizeZ = imgPlus.averageScale( 2 );

			final T min = imgPlus.firstElement().createVariable();
			final T max = imgPlus.firstElement().createVariable();
			ComputeMinMax.computeMinMax( imgPlus, min, max );
			this.minIntensity = min.getRealDouble();
			this.maxIntensity = max.getRealDouble();
		}
	}
}
