package de.mpicbg.jug.clearvolume.gui;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;

public class ClearVolumeManager< T extends RealType< T > & NativeType< T >> implements Runnable {

	private ClearVolumeRendererInterface cv;
	private final List< RandomAccessibleInterval< T >> images;
	private final int numChannels;
	private int activeChannelIndex;

	private int maxTextureWidth;
	private int maxTextureHeight;

	private double voxelSizeX;
	private double voxelSizeY;
	private double voxelSizeZ;

	private double[] minIntensities;
	private double[] maxIntensities;

	/**
	 * @param ctnrClearVolume
	 */
	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow ) {
		this( imagesToShow, 512, 512 );
	}

	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow,
			final int maxTextureWidth,
			final int maxTextureHeight ) {

		this.images = imagesToShow;
		this.numChannels = images.size();
		this.setActiveChannelIndex( 0 );

		this.maxTextureWidth = maxTextureWidth;
		this.maxTextureHeight = maxTextureHeight;

		this.voxelSizeX = 1.;
		this.voxelSizeY = 1.;
		this.voxelSizeZ = 1.;

		this.minIntensities = new double[ numChannels ];
		this.maxIntensities = new double[ numChannels ];
		for ( int i = 0; i < numChannels; i++ ) {
			final T min = images.get( i ).randomAccess().get().createVariable();
			final T max = images.get( i ).randomAccess().get().createVariable();
			ComputeMinMax.computeMinMax( images.get( i ), min, max );
			minIntensities[ i ] = min.getRealDouble();
			maxIntensities[ i ] = max.getRealDouble();
		}
	}

	@Override
	public void run() {
		cv = ImgLib2ClearVolume.initRealImgs( images, "Generic ClearVolume GUI",
				maxTextureWidth, maxTextureHeight,
				maxTextureWidth, maxTextureHeight,
				true,
				minIntensities,
				maxIntensities );
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

	public void setIntensityValues(
			final int channelIndex,
			final double minIntensity,
			final double maxIntensity ) {
		minIntensities[ channelIndex ] = minIntensity;
		maxIntensities[ channelIndex ] = maxIntensity;
	}

	public void setVoxelSize(
			final double voxelSizeX,
			final double voxelSizeY,
			final double voxelSizeZ ) {
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
		this.maxTextureHeight = textureHeight;
		this.maxTextureWidth = textureWidth;
	}

	public int getTextureWidth() {
		return this.maxTextureWidth;
	}

	public int getTextureHeight() {
		return this.maxTextureHeight;
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

	public double getMinIntensity( final int channelIndex ) {
		return minIntensities[ channelIndex ];
	}

	public double getMaxIntensity( final int channelIndex ) {
		return maxIntensities[ channelIndex ];
	}

	public double[] getMinIntensities() {
		return minIntensities;
	}

	public double[] getMaxIntensities() {
		return maxIntensities;
	}

	/**
	 * @return the activeChannelIndex
	 */
	public int getActiveChannelIndex() {
		return activeChannelIndex;
	}

	/**
	 * @param activeChannelIndex
	 *            the activeChannelIndex to set
	 */
	public void setActiveChannelIndex( final int activeChannelIndex ) {
		this.activeChannelIndex = activeChannelIndex;
	}
}
