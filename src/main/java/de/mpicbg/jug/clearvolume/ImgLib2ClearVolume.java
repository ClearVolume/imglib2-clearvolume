/**
 *
 */
package de.mpicbg.jug.clearvolume;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunction;
import clearvolume.transferf.TransferFunctions;
import de.mpicbg.jug.imglib2.converter.RealClearVolumeUnsignedShortConverter;

/**
 * @author jug
 */
/**
 * @author jug
 */
public class ImgLib2ClearVolume {

	/**
	 * Initializes a ClearVolume window for some ArrayImgs of ByteType,
	 * ByteArray.
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface initByteArrayImgs(
			final List< ArrayImg< ByteType, ByteArray > > channelImgs,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer =
				ClearVolumeRendererFactory.newBestRenderer(
						pWindowName,
						pWindowWidth,
						pWindowHeight,
						1,
						pMaxTextureWidth,
						pMaxTextureHeight,
						channelImgs.size(),
						useInCanvas );
		for ( int channel = 0; channel < channelImgs.size(); channel++ ) {
			lClearVolumeRenderer.setCurrentRenderLayer( channel );
			final byte[] bytes = channelImgs.get( channel ).update( null ).getCurrentStorageArray();
			lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
					channelImgs.get( channel ).dimension( 0 ),
					channelImgs.get( channel ).dimension( 1 ),
					channelImgs.get( channel ).dimension( 2 ) );
			lClearVolumeRenderer.setTransferFunction(
					channel,
					TransferFunctions.getGradientForColor( channel ) );
		}
		return lClearVolumeRenderer;
	}

	/**
	 * Shows ArrayImgs of ByteType, ByteArray in ClearVolume.
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface showByteArrayImgs(
			final List< ArrayImg< ByteType, ByteArray > > channelImgs,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas ) {
		final ClearVolumeRendererInterface cv =
				initByteArrayImgs( channelImgs, pWindowName, pWindowWidth, pWindowHeight,
						pMaxTextureWidth, pMaxTextureHeight, useInCanvas );
//		cv.requestDisplay();
		return cv;
	}

	/**
	 * Initializes a ClearVolume window for some ArrayImgs of UnsignedShortType,
	 * ShortArray.
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface initUnsignedShortArrayImgs(
			final List< ArrayImg< UnsignedShortType, ShortArray > > channelImgs,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer =
				ClearVolumeRendererFactory.newBestRenderer(
						pWindowName,
						pWindowWidth,
						pWindowHeight,
						2,
						pMaxTextureWidth,
						pMaxTextureHeight,
						channelImgs.size(),
						useInCanvas );
		for ( int channel = 0; channel < channelImgs.size(); channel++ ) {
			lClearVolumeRenderer.setCurrentRenderLayer( channel );

			// get the byte array out of the Img<ByteArray>
			final short[] shorts =
					channelImgs.get( channel ).update( null ).getCurrentStorageArray();
			final byte[] bytes = new byte[ shorts.length * 2 ];
			int i = 0;
			for ( final short s : shorts ) {
				bytes[ i ] = ( byte ) ( s & 0xff );
				bytes[ i + 1 ] = ( byte ) ( ( s >> 8 ) & 0xff );
				i += 2;
			}
			lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
					channelImgs.get( channel ).dimension( 0 ),
					channelImgs.get( channel ).dimension( 1 ),
					channelImgs.get( channel ).dimension( 2 ) );
			lClearVolumeRenderer.setTransferFunction(
					channel,
					getTransferFunctionForChannel( channel ) );
		}
		return lClearVolumeRenderer;
	}

	/**
	 * Shows ArrayImgs of UnsignedShortType, ShortArray in ClearVolume.
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface showUnsignedShortArrayImgs(
			final List< ArrayImg< UnsignedShortType, ShortArray > > channelImgs,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas ) {
		final ClearVolumeRendererInterface cv =
				initUnsignedShortArrayImgs( channelImgs, pWindowName, pWindowWidth, pWindowHeight,
						pMaxTextureWidth, pMaxTextureHeight, useInCanvas );
//		cv.requestDisplay();
		return cv;
	}

	/**
	 * Initializes a ClearVolume window for some ArrayImgs of
	 * ClearVolumeUnsignedShortType, ByteArray.
	 * This method does NOT duplicate the image, but works directly on the
	 * ArrayImg data.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface initClearVolumeUnsignedShortArrayImg(
			final List< ArrayImg< ClearVolumeUnsignedShortType, ByteArray > > channelImages,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas,
			final boolean useCuda ) {
		ClearVolumeRendererInterface lClearVolumeRenderer = null;
		if ( useCuda ) {
			lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
						pWindowName,
						pWindowWidth,
						pWindowHeight,
						2,
						pMaxTextureWidth,
						pMaxTextureHeight,
						channelImages.size(),
						useInCanvas );
		} else {
			lClearVolumeRenderer = ClearVolumeRendererFactory.newOpenCLRenderer(
					pWindowName,
					pWindowWidth,
					pWindowHeight,
					2,
					pMaxTextureWidth,
					pMaxTextureHeight,
					channelImages.size(),
					useInCanvas );
		}
		for ( int channel = 0; channel < channelImages.size(); channel++ ) {
			lClearVolumeRenderer.setCurrentRenderLayer( channel );
			final byte[] bytes =
					channelImages.get( channel ).update( null ).getCurrentStorageArray();
			lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
					channelImages.get( channel ).dimension( 0 ),
					channelImages.get( channel ).dimension( 1 ),
					channelImages.get( channel ).dimension( 2 ) );
			lClearVolumeRenderer.setTransferFunction(
					channel,
					getTransferFunctionForChannel( channel ) );
		}
		return lClearVolumeRenderer;
	}

	/**
	 * Shows some ArrayImgs of type ClearVolumeUnsignedShortType.
	 * This method does NOT duplicate the image, but works directly on the
	 * ArrayImg data.
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static ClearVolumeRendererInterface showClearVolumeUnsignedShortArrayImgWindow(
			final List< ArrayImg< ClearVolumeUnsignedShortType, ByteArray > > channelImgs,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final boolean useInCanvas,
			final boolean useCuda ) {
		final ClearVolumeRendererInterface cv =
				initClearVolumeUnsignedShortArrayImg( channelImgs, pWindowName, pWindowWidth,
						pWindowHeight, pMaxTextureWidth, pMaxTextureHeight, useInCanvas, useCuda );
		cv.requestDisplay();
		return cv;
	}

	/**
	 * @param channelImages
	 * @param min
	 * @param max
	 * @return
	 */
	public static < ST extends RealType< ST > & NativeType< ST > >
			List< ArrayImg< ClearVolumeUnsignedShortType, ByteArray >>
			makeClearVolumeUnsignedShortTypeCopies(
					final List< RandomAccessibleInterval< ST >> channelImages,
					final double[] min,
					final double[] max ) {

		final List< ArrayImg< ClearVolumeUnsignedShortType, ByteArray >> ret =
				new ArrayList< ArrayImg< ClearVolumeUnsignedShortType, ByteArray >>();

		final int minLen = Math.min( Math.min( min.length, max.length ), channelImages.size() );
		for ( int i = 0; i < minLen; i++ ) {
			ret.add( makeClearVolumeUnsignedShortTypeCopy(
					channelImages.get( i ),
					min[ i ],
					max[ i ] ) );
		}
		return ret;
	}

	/**
	 *
	 * @param source
	 * @param min
	 * @param max
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static < ST extends RealType< ST > & NativeType< ST > >
			ArrayImg< ClearVolumeUnsignedShortType, ByteArray >
			makeClearVolumeUnsignedShortTypeCopy(
					final RandomAccessibleInterval< ST > source,
					final double min,
					final double max ) {
		final int srcNumDims = source.numDimensions();
		final long[] srcDims = new long[ srcNumDims ];
		source.dimensions( srcDims );
		final ArrayImg< ClearVolumeUnsignedShortType, ? > target =
				new ArrayImgFactory< ClearVolumeUnsignedShortType >().create( srcDims,
						new ClearVolumeUnsignedShortType() );

		copy( source, target, new RealClearVolumeUnsignedShortConverter< ST >( min, max ) );

		return ( ArrayImg< ClearVolumeUnsignedShortType, ByteArray > ) target;
	}

	/**
	 *
	 * @param source
	 * @param target
	 * @param converter
	 */
	private static < T1 extends Type< T1 >, T2 extends Type< T2 >> void copy(
			final RandomAccessible< T1 > source,
			final IterableInterval< T2 > target,
			final Converter< T1, T2 > converter ) {
		// create a cursor that automatically localizes itself on every move
		final Cursor< T2 > targetCursor = target.localizingCursor();
		final RandomAccess< T1 > sourceRandomAccess = source.randomAccess();

		// iterate over the input cursor
		while ( targetCursor.hasNext() ) {
			// move input cursor forward
			targetCursor.fwd();

			// set the output cursor to the position of the input cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set converted value
			converter.convert( sourceRandomAccess.get(), targetCursor.get() );
		}
	}

	/**
	 * Initializes a ClearVolume window for some Imgs of R extends RealType and
	 * NativeType.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param channelImages
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface
			initRealImgs(
					final List< RandomAccessibleInterval< R >> channelImages,
					final String pWindowName,
					final int pWindowWidth,
					final int pWindowHeight,
					final int pMaxTextureWidth,
					final int pMaxTextureHeight,
					final boolean useInCanvas,
					final double[] min,
					final double[] max,
					final boolean useCuda ) {
		return initClearVolumeUnsignedShortArrayImg(
				makeClearVolumeUnsignedShortTypeCopies( channelImages,
						min,
						max ),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight,
				useInCanvas,
				useCuda );
	}

	/**
	 * Can show any RealType Img in ClearVolume.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param channelImgs
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @param useInCanvas
	 *            must be set true if you will use ClearVolume embedded in an
	 *            AWT or Swing container.
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface
			showRealImgs(
					final List< RandomAccessibleInterval< R > > channelImgs,
					final String pWindowName,
					final int pWindowWidth,
					final int pWindowHeight,
					final int pMaxTextureWidth,
					final int pMaxTextureHeight,
					final boolean useInCanvas,
					final double[] min,
					final double[] max,
					final boolean useCuda ) {
		return showClearVolumeUnsignedShortArrayImgWindow(
				makeClearVolumeUnsignedShortTypeCopies( channelImgs, min, max ),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight,
				useInCanvas,
				useCuda );
	}

	/**
	 * @param channel
	 * @return
	 */
	private static TransferFunction getTransferFunctionForChannel( final int channel ) {
		switch ( channel % 4 ) {
		case 0:
			return TransferFunctions.getGrayLevel();
		case 1:
			return TransferFunctions.getGradientForColor( 2 );
		case 2:
			return TransferFunctions.getGradientForColor( 3 );
		case 3:
			return TransferFunctions.getGradientForColor( 1 );
		}
		return TransferFunctions.getGrayLevel();
	}

	public static Img< FloatType > makeImgFromBytes(
			final long pVolumeWidth,
			final long pVolumeHeight,
			final long pVolumeDepth,
			final int pBytesPerVoxel,
			final boolean pFloatType,
			final ByteBuffer[] pCaptureBuffers ) {

		final ArrayImg< FloatType, FloatArray > img =
				ArrayImgs.floats( pVolumeWidth, pVolumeHeight, pCaptureBuffers.length, pVolumeDepth );
//		final RandomAccess< ShortType > raImg = img.randomAccess();

		final Cursor< FloatType > targetCursor = img.localizingCursor();
		while ( targetCursor.hasNext() ) {
			targetCursor.fwd();

			final long x = targetCursor.getLongPosition( 0 );
			final long y = targetCursor.getLongPosition( 1 );
			final int c = ( int ) targetCursor.getLongPosition( 2 );
			final long z = targetCursor.getLongPosition( 3 );
			int index = ( int ) ( ( z * pVolumeWidth * pVolumeHeight ) + ( y * pVolumeWidth ) + x );
			index *= pBytesPerVoxel;

			float value = 0;
			if ( pBytesPerVoxel == 1 ) {
				value = pCaptureBuffers[ c ].get( index );
			} else if ( pBytesPerVoxel == 2 ) {
//				if ( pFloatType ) {
//					value = pCaptureBuffers[ c ].getFloat( index );
//				} else {
					value = pCaptureBuffers[ c ].getChar( index );
//				}
			} else {
				throw new RuntimeException( "Capture feature does only support 1 or 2 bytes per voxel." );
			}

			// set converted value
			targetCursor.get().set( value );
		}

		return img;
	}
}
