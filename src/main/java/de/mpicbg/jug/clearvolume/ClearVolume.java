/**
 *
 */
package de.mpicbg.jug.clearvolume;

import java.nio.ByteBuffer;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;
import de.mpicbg.jug.imglib2.converter.RealClearVolumeUnsignedShortConverter;


/**
 * @author jug
 */
public class ClearVolume {

	/**
	 * Initializes a ClearVolume window for an ArrayImg< ByteType, ByteArray >.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface initByteArrayImgWindow(
			final ArrayImg< ByteType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				1,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final byte[] bytes = imgVolumeDataArray.update( null ).getCurrentStorageArray();

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));
		lClearVolumeRenderer.requestDisplay();

		return lClearVolumeRenderer;
	}

	/**
	 * Shows ArrayImg< ByteType, ByteArray > in ClearVolume window.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface showByteArrayImgWindow(
			final ArrayImg< ByteType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {
		final ClearVolumeRendererInterface cv = initByteArrayImgWindow( imgVolumeDataArray, pWindowName, pWindowWidth, pWindowHeight, pMaxTextureWidth, pMaxTextureHeight );
		cv.requestDisplay();
		return cv;
	}

	/**
	 * Initializes a ClearVolume window for an ArrayImg< UnsignedShortType, ShortArray >.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface initUnsignedShortArrayImgWindow(
			final ArrayImg< UnsignedShortType, ShortArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				2,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final short[] shorts = imgVolumeDataArray.update( null ).getCurrentStorageArray();
		final byte[] bytes = new byte[ shorts.length * 2 ];

		int i = 0;
		for ( final short s : shorts ) {
			bytes[ i ] = ( byte ) ( s & 0xff );
			bytes[ i + 1 ] = ( byte ) ( ( s >> 8 ) & 0xff );
			i += 2;
		}

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));

		return lClearVolumeRenderer;
	}

	/**
	 * Shows ArrayImg< UnsignedShortType, ShortArray > in ClearVolume window.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface showUnsignedShortArrayImgWindow(
			final ArrayImg< UnsignedShortType, ShortArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {
		final ClearVolumeRendererInterface cv = initUnsignedShortArrayImgWindow( imgVolumeDataArray, pWindowName, pWindowWidth, pWindowHeight, pMaxTextureWidth, pMaxTextureHeight );
		cv.requestDisplay();
		return cv;
	}

	/**
	 * Initializes a ClearVolume window for an ArrayImg< ClearVolumeUnsignedShortType, ByteArray >.
	 * This method does NOT dupicate the image, but works directly on the
	 * ArrayImg data.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface initClearVolumeUnsignedShortArrayImgWindow(
			final ArrayImg< ClearVolumeUnsignedShortType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {
		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				2,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final byte[] bytes = imgVolumeDataArray.update( null ).getCurrentStorageArray();

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));

		return lClearVolumeRenderer;
	}

	/**
	 * Shows a ArrayImg of type ClearVolumeUnsignedShortType.
	 * This method does NOT dupicate the image, but works directly on the
	 * ArrayImg data.
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static ClearVolumeRendererInterface showClearVolumeUnsignedShortArrayImgWindow(
			final ArrayImg< ClearVolumeUnsignedShortType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {
		final ClearVolumeRendererInterface cv = initClearVolumeUnsignedShortArrayImgWindow( imgVolumeDataArray, pWindowName, pWindowWidth, pWindowHeight, pMaxTextureWidth, pMaxTextureHeight );
		cv.requestDisplay();
		return cv;
	}

	/**
	 * Initializes a ClearVolume window for an ArrayImg< R, ? >.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface initRealArrayImgWindow(
			final ArrayImg< R, ? > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final double min,
			final double max) {
		return initClearVolumeUnsignedShortArrayImgWindow(
				makeClearVolumeUnsignedShortTypeCopy(imgVolumeDataArray, min, max),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight );
	}

	/**
	 * Shows any RealType ArrayImg in ClearVolume.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface showRealArrayImg(
			final ArrayImg< R, ? > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final double min,
			final double max) {
		return showClearVolumeUnsignedShortArrayImgWindow(
				makeClearVolumeUnsignedShortTypeCopy(imgVolumeDataArray, min, max),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight );
	}

	@SuppressWarnings( "unchecked" )
	public static < ST extends RealType< ST > & NativeType< ST > > ArrayImg< ClearVolumeUnsignedShortType, ByteArray >
	makeClearVolumeUnsignedShortTypeCopy( final RandomAccessibleInterval< ST > source, final double min, final double max ) {
		final ST sourceType = source.randomAccess().get();
		final ArrayImg< ClearVolumeUnsignedShortType, ? > target =
				new ArrayImgFactory< ClearVolumeUnsignedShortType >().create( new long[] {source.dimension( 0 ), source.dimension( 1 ), source.dimension( 2 )},
						new ClearVolumeUnsignedShortType() );

		final Cursor< ClearVolumeUnsignedShortType > targetCursor = target.localizingCursor();
		final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
		copy( source, target, new RealClearVolumeUnsignedShortConverter< ST >( min, max ) );

		return ( ArrayImg< ClearVolumeUnsignedShortType, ByteArray > ) target;
	}

	private static < T1 extends Type< T1 >, T2 extends Type< T2 >> void copy( final RandomAccessible< T1 > source, final IterableInterval< T2 > target, final Converter< T1, T2 > converter ) {
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
	 * Initializes a ClearVolume window for an Img< R extends RealType & NativeType >.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface initRealImg(
			final Img< R > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final double min,
			final double max) {
		return initClearVolumeUnsignedShortArrayImgWindow(
				makeClearVolumeUnsignedShortTypeCopy(imgVolumeDataArray, min, max),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight );
	}

	/**
	 * Can show any RealType Img in ClearVolume.
	 * Note: any given image will be duplicated in memory!
	 *
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > > ClearVolumeRendererInterface showRealImg(
			final Img< R > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight,
			final double min,
			final double max) {
		return showClearVolumeUnsignedShortArrayImgWindow(
				makeClearVolumeUnsignedShortTypeCopy(imgVolumeDataArray, min, max),
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				pMaxTextureWidth,
				pMaxTextureHeight );
	}

}
