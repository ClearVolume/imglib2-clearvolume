package de.mpicbg.jug.clearvolume.gui;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import de.mpicbg.jug.clearvolume.ClearVolume;

public class ClearVolumeManager< T extends RealType< T > & NativeType< T >> implements Runnable {

	private ClearVolumeRendererInterface cv;
	private List< RandomAccessibleInterval< T >> images;;

	private int textureWidth;
	private int textureHeight;
	private double minIntensity;
	private double maxIntensity;
	private double voxelSizeX;
	private double voxelSizeY;
	private double voxelSizeZ;

	/**
	 * @param ctnrClearVolume
	 */
	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow ) {
		this( imagesToShow, 512, 512 );
	}

	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow, final int textureWidth, final int textureHeight ) {

		this.images = imagesToShow;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.minIntensity = 0.;
		this.maxIntensity = 255;
		this.voxelSizeX = 1.;
		this.voxelSizeY = 1.;
		this.voxelSizeZ = 1.;
	}

	@Override
	public void run() {
		cv = ClearVolume.initRealImgs( images, "Generic ClearVolume GUI",
									  512, 512,
									  textureWidth, textureHeight,
									  true,
									  minIntensity, maxIntensity );
		cv.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
	}

	public void resetView() {
		cv.resetRotationTranslation();
		cv.resetBrightnessAndGammaAndTransferFunctionRanges();
		updateView();
	}

	public void updateView() {
		cv.notifyUpdateOfVolumeRenderingParameters();
		cv.requestDisplay();
	}

	public void close() {
		if ( cv != null ) {
			cv.close();
		}
	}

	public ClearVolumeRendererInterface getClearVolumeRendererInterface() {
		return cv;
	}

	public List< RandomAccessibleInterval< T >> getChannelImages() {
		return images;
	}

	public void toggleBox() {
		cv.toggleBoxDisplay();
		updateView();
	}

	public void toggleRecording() {
		cv.toggleRecording();
	}

	public void setIntensityValues( final double minIntensity, final double maxIntensity ) {
		this.minIntensity = minIntensity;
		this.maxIntensity = maxIntensity;
	}

	public void setVoxelSize( final double voxelSizeX, final double voxelSizeY, final double voxelSizeZ ) {
		this.voxelSizeX = voxelSizeX;
		this.voxelSizeY = voxelSizeY;
		this.voxelSizeZ = voxelSizeZ;
		if ( cv != null )
			cv.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
	}

	/**
	 * Sets the size of the texture to use. Calling this method has an effect
	 * only before <code>run()</code> is called for the first time!
	 *
	 * @param textureWidth
	 * @param textureHeight
	 */
	public void setTextureSize( final int textureWidth, final int textureHeight ) {
		this.textureHeight = textureHeight;
		this.textureWidth = textureWidth;
	}

	public int getTextureWidth() {
		return this.textureWidth;
	}

	public int getTextureHeight() {
		return this.textureHeight;
	}

	public double getMinIntensity() {
		return this.minIntensity;
	}

	public double getMaxIntensity() {
		return this.maxIntensity;
	}

	public double getVoxelSizeX() {
		return this.voxelSizeX;
	}

	public double getVoxelSizeY() {
		return this.voxelSizeY;
	}

	public double getVoxelSizeZ() {
		return this.voxelSizeZ;
	}
}