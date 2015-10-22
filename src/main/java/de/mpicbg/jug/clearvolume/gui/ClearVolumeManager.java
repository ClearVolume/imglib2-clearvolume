package de.mpicbg.jug.clearvolume.gui;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.transferf.TransferFunction;
import de.mpicbg.jug.clearvolume.ClearVolumeUnsignedShortType;
import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;

public class ClearVolumeManager< T extends RealType< T > & NativeType< T >> implements Runnable {

	private ClearVolumeRendererInterface cv;
	private List< RandomAccessibleInterval< T >> images;
	private final int numChannels;

	private int activeChannelIndex;
	private List< ActiveLayerListener > activeLayerChangedListeners;

	private int maxTextureWidth;
	private int maxTextureHeight;

	private double voxelSizeX;
	private double voxelSizeY;
	private double voxelSizeZ;

	private double[] minIntensities;
	private double[] maxIntensities;

	private boolean useCuda = true;

	/**
	 * @param ctnrClearVolume
	 */
	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow ) {
		this( imagesToShow, 758, 768, true );
	}

	public ClearVolumeManager( final List< RandomAccessibleInterval< T >> imagesToShow,
			final int maxTextureWidth,
			final int maxTextureHeight,
			final boolean useCuda ) {

		this.maxTextureWidth = maxTextureWidth;
		this.maxTextureHeight = maxTextureHeight;

		this.useCuda = useCuda;

		this.voxelSizeX = 1.;
		this.voxelSizeY = 1.;
		this.voxelSizeZ = 1.;

		activeLayerChangedListeners = new ArrayList< ActiveLayerListener >();
		this.setActiveChannelIndex( 0 );
		this.numChannels = imagesToShow.size();

		this.images = null;
		setImages( imagesToShow );
	}

	/**
	 * @param imagesToShow
	 */
	private void setImages( final List< RandomAccessibleInterval< T >> imagesToShow ) {
		this.images = imagesToShow;

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

	/**
	 * Updates the currently displayed channel images by the given ones.
	 *
	 * @param imagesToShow
	 * @return true if the update was successful
	 */
	public boolean updateImages( final List< RandomAccessibleInterval< T >> imagesToShow ) {
		return updateImages( imagesToShow, true );
	}

	/**
	 * Updates the currently displayed channel images by the given ones.
	 *
	 * @param imagesToShow
	 * @param doNormalize
	 *
	 * @return true if the update was successful
	 */
	public boolean updateImages(
			final List< RandomAccessibleInterval< T >> imagesToShow,
			final boolean doNormalize ) {

		if ( images.size() != imagesToShow.size() ) { return false; }

		int c = 0;
		if ( doNormalize ) {
			this.minIntensities = new double[ imagesToShow.size() ];
			this.maxIntensities = new double[ imagesToShow.size() ];
			for ( final RandomAccessibleInterval< T > img : imagesToShow ) {
				final T min = img.randomAccess().get().createVariable();
				final T max = img.randomAccess().get().createVariable();
				ComputeMinMax.computeMinMax( img, min, max );
				minIntensities[ c ] = min.getRealDouble();
				maxIntensities[ c ] = max.getRealDouble();
				c++;
			}
		}

		final List< ArrayImg< ClearVolumeUnsignedShortType, ByteArray >> converted =
				ImgLib2ClearVolume.makeClearVolumeUnsignedShortTypeCopies(
						imagesToShow,
						minIntensities,
						maxIntensities );

		cv.setVolumeDataUpdateAllowed(false);
		c = 0;
		for ( final RandomAccessibleInterval< T > img : imagesToShow ) {
			final int sizeX = ( int ) ( img.dimension( 0 ) );
			final int sizeY = ( int ) ( img.dimension( 1 ) );
			final int sizeZ = ( int ) ( img.dimension( 2 ) );
			final byte[] bytes =
					converted.get( c ).update( null ).getCurrentStorageArray();
			cv.setVolumeDataBuffer(
					c,
					ByteBuffer.wrap( bytes ),
					sizeX,
					sizeY,
					sizeZ,
					voxelSizeX,
					voxelSizeY,
					voxelSizeZ );
			c++;
		}
		cv.setVolumeDataUpdateAllowed(true);

		return true;
	}

	@Override
	public void run() {
		cv = ImgLib2ClearVolume.initRealImgs( images, "Generic ClearVolume GUI",
				maxTextureWidth, maxTextureHeight,
				maxTextureWidth, maxTextureHeight,
				true,
				minIntensities,
				maxIntensities,
				useCuda );
		cv.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
	}

	public void resetView() {
		cv.resetRotationTranslation();
		cv.resetBrightnessAndGammaAndTransferFunctionRanges();
		updateView();
	}

	public void updateView() {
		cv.notifyChangeOfVolumeRenderingParameters();
		cv.requestDisplay();
	}

	public void close() {
		if ( cv != null ) {
			try {
				final Runnable todo = new Runnable() {

					@Override
					public void run() {
						cv.close();
					}
				};

				if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
					todo.run();
				} else {
					SwingUtilities.invokeAndWait( todo );
				}
			} catch ( final Exception e ) {
				System.err.println( "Closing of CV session was interrupted in ClearVolumeManager!" );
			}
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
		if ( cv != null )
			cv.setCurrentRenderLayer( activeChannelIndex );
		this.activeChannelIndex = activeChannelIndex;
		throwActiveLayerChanged();
	}

	/**
	 * Notifies all registered ActiveLayerListener about the change.
	 */
	private void throwActiveLayerChanged() {
		for ( final ActiveLayerListener l : activeLayerChangedListeners ) {
			l.activeLayerChanged( this.activeChannelIndex );
		}
	}

	/**
	 * @param l
	 *            listener to be added.
	 */
	public void addActiveLayerChangedListener( final ActiveLayerListener l ) {
		activeLayerChangedListeners.add( l );
	}

	/**
	 * @param channelId
	 * @return
	 */
	public boolean isChannelVisible( final int channelId ) {
		return cv.isLayerVisible( channelId );
	}

	/**
	 * @param channelId
	 * @param visible
	 */
	public void setChannelVisible( final int channelId, final boolean visible ) {
		cv.setLayerVisible( channelId, visible );
		cv.requestDisplay();
	}

	/**
	 * @param channelId
	 * @return
	 */
	public double getBrightness( final int channelId ) {
		return cv.getBrightness( channelId );
	}

	/**
	 * @param channelId
	 * @param brightness
	 */
	public void setBrightness( final int channelId, final double brightness ) {
		cv.setBrightness( channelId, brightness );
		cv.requestDisplay();
	}

	/**
	 * @return
	 */
	public Icon getTransferFunctionColorIcon( final int channelId ) {
		final TransferFunction tf = cv.getTransferFunction( channelId );
		return new TransferFunctionGradientIcon( 20, 20, tf );
	}

	/**
	 * @param channelId
	 * @param gradientForColor
	 */
	public void setTransferFunction( final int channelId, final TransferFunction transferFunction ) {
		cv.setTransferFunction( channelId, transferFunction );
	}

	/**
	 * @param channelId
	 */
	public TransferFunction getTransferFunction( final int channelId ) {
		return cv.getTransferFunction( channelId );
	}

	/**
	 * @param b
	 */
	public void setCuda( final boolean b ) {
		this.useCuda = b;
	}
}
